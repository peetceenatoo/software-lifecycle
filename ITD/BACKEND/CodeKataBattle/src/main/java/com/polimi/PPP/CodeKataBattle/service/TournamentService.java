package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import com.polimi.PPP.CodeKataBattle.TaskScheduling.DeadlineScheduler;
import com.polimi.PPP.CodeKataBattle.TaskScheduling.TournamentCreatedEvent;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import com.polimi.PPP.CodeKataBattle.Utilities.TimezoneUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final BattleRepository battleRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Qualifier("emailProvider")
    private final NotificationProvider notificationProvider;

    @Autowired
    public TournamentService(TournamentRepository tournamentRepository, BattleRepository battleRepository, UserRepository userRepository, ModelMapper modelMapper, ApplicationEventPublisher eventPublisher,
                             NotificationProvider notificationProvider, RoleRepository roleRepository){
        this.eventPublisher = eventPublisher;
        this.tournamentRepository = tournamentRepository;
        this.battleRepository = battleRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.notificationProvider = notificationProvider;
        this.roleRepository = roleRepository;

    }

    public Boolean hasUserRightsOnTournament(Long userId, Long tournamentId){
        return tournamentRepository.hasUserRightsOnTournament(userId, tournamentId);
    }

    public TournamentDTO getTournamentById(Long tournamentId) {
        return modelMapper.map(tournamentRepository.findById(tournamentId).orElseThrow(() -> new IllegalArgumentException("Invalid Tournament ID")), TournamentDTO.class);
    }

    public List<TournamentDTO> getTournaments(TournamentStateEnum state) {
        if (state != null) {
            return tournamentRepository.findByState(state)
                                       .stream()
                                       .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                                       .collect(Collectors.toList());
        }
        return tournamentRepository.findAll()
                                   .stream()
                                   .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                                   .collect(Collectors.toList());
    }

    public List<TournamentDTO> getManagedTournaments(Long educatorId) {

        return tournamentRepository.findByUsers_Id(educatorId).stream()
            .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
            .collect(Collectors.toList());
    }

    public List<TournamentDTO> getEnrolledTournaments(Long studentId) {
        return tournamentRepository.findByUsers_Id(studentId).stream()
            .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
            .collect(Collectors.toList());
    }

    public List<TournamentDTO> getTournamentsToSchedule() {
        return tournamentRepository.findByState(TournamentStateEnum.SUBSCRIPTION).stream()
                                   .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                                   .collect(Collectors.toList());
    }

    public TournamentDTO updateStateForTournament(Long tournamentId, TournamentStateEnum newState) {
        // No check on educator role as it is done by the scheduler
        Tournament tournament = tournamentRepository.findById(tournamentId)
                                                    .orElseThrow(() -> new IllegalArgumentException("Tournament ID is wrong"));

        if(tournament.getState().ordinal() >= newState.ordinal()){
            throw new IllegalStateException("Cannot put tournament in a previous state.");
        }

        tournament.setState(newState);
        Tournament updated = tournamentRepository.save(tournament);
        return modelMapper.map(updated, TournamentDTO.class);
    }

    @Transactional
    public TournamentDTO createTournament(TournamentCreationDTO tournamentDTO) {

        if (tournamentDTO.getEducatorsInvited().isEmpty()){
            throw new IllegalArgumentException("At least one educator must be invited (the creator)");
        }

        // Input validation done in the controller
        Tournament tournament = new Tournament();
        modelMapper.map(tournamentDTO, tournament);

        ZonedDateTime utcDeadline = TimezoneUtil.convertToUtc(tournamentDTO.getRegistrationDeadline());

        if(utcDeadline.isBefore(ZonedDateTime.now())){
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }

        tournament.setDeadline(utcDeadline);
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);
        tournament.setBattles(new java.util.HashSet<>());
        tournament.setUsers(new java.util.HashSet<>());

        tournament = tournamentRepository.save(tournament);

        // Handle association with educators
        for (Long id : tournamentDTO.getEducatorsInvited()) {
            User educator = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("EducatorId not valid"));
            educator.getTournaments().add(tournament);
            userRepository.save(educator);

            tournament.getUsers().add(educator);
        }

        // Save tournament
        Tournament savedTournament = tournamentRepository.save(tournament);

        TournamentDTO toBeReturned = modelMapper.map(savedTournament, TournamentDTO.class);

        // Register an after-commit lambda
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {


                eventPublisher.publishEvent(new TournamentCreatedEvent(this,toBeReturned));

                // Send email to students
                Role studentRole = roleRepository.findByName(RoleEnum.ROLE_STUDENT).get();
                List<User> students = userRepository.findByRole(studentRole);
                List<String> studentsEmail = students.stream().map(User::getEmail).toList();

                MessageDTO messageDTO = new MessageDTO("The new tournament '" + savedTournament.getName() + "' has been created, check it out.", "New tournament created");
                notificationProvider.sendNotification(messageDTO, studentsEmail);
            }

            // Implement other methods as needed or leave them as default
        });

        return toBeReturned;
    }

    public TournamentDTO closeTournament(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                                                    .orElseThrow(() -> new IllegalArgumentException("Tournament ID is wrong"));

        if(!tournamentRepository.hasUserRightsOnTournament(userId, tournamentId)){
            throw new IllegalArgumentException("User does not have rights on this tournament");
        }

        if (tournament.getState() != TournamentStateEnum.ONGOING) {
            throw new IllegalStateException("Tournament is not in ongoing phase");
        }

        List<Battle> battles = battleRepository.findByTournamentId(tournamentId);
        boolean allBattlesEnded = battles.stream().allMatch(battle -> battle.getState() == BattleStateEnum.ENDED);
    
        if (!allBattlesEnded) {
            throw new IllegalStateException("There are still Battles ongoing or in consolidation stage.");
        }
    
        tournament.setState(TournamentStateEnum.ENDED);
        Tournament result = tournamentRepository.save(tournament);

        List<String> studentsEmails = result.getUsers().stream().filter(s -> s.getRole().getName() == RoleEnum.ROLE_STUDENT).map(User::getEmail).toList();
        MessageDTO messageDTO = new MessageDTO("The tournament '" + result.getName() + "' has ended, check it out.", "Tournament ended");
        notificationProvider.sendNotification(messageDTO, studentsEmails);

        return modelMapper.map(result, TournamentDTO.class);
    }

    @Transactional
    public void enrollUserInTournament(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (tournament.getState() != TournamentStateEnum.SUBSCRIPTION) {
            throw new IllegalStateException("Tournament is not in subscription phase");
        }

        tournament.getUsers().add(user);
        user.getTournaments().add(tournament);

        userRepository.save(user);
        tournamentRepository.save(tournament);
    }

    public List<TournamentRankingDTO> getTournamentRanking(Long tournamentId){

        if (tournamentRepository.findById(tournamentId).isEmpty()){
            throw new IllegalArgumentException("Tournament not found");
        }

        List<Object[]> results = tournamentRepository.calculateStudentRankingForTournament(tournamentId);
        List<TournamentRankingDTO> rankings = new ArrayList<>();
        for (Object[] result : results) {
            String username = (String) result[0];
            Long totalScore = ((Number) result[1]).longValue(); // Convert to Long, assuming SUM returns a numeric type
            rankings.add(new TournamentRankingDTO(username, totalScore));
        }
        return rankings;
    }

    public List<TournamentDTO> searchTournamentsByKeywordAndState(String keyword, TournamentStateEnum state) {

        long id = -1L;
        try {
            id = Long.parseLong(keyword);
        } catch (NumberFormatException e) {
            // Do nothing
        }

        List<Tournament> tournaments = tournamentRepository.findByNameContainingIgnoreCaseOrIdIsAndState(keyword, id, state);
        return tournaments.stream()
                .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                .collect(Collectors.toList());

    }

    public List<TournamentDTO> searchTournamentsByKeyword(String keyword) {

        long id = -1L;
        try {
            id = Long.parseLong(keyword);
        } catch (NumberFormatException e) {
            // Do nothing
        }

        List<Tournament> tournaments = tournamentRepository.findByNameContainingIgnoreCaseOrIdIs(keyword, id);
        return tournaments.stream()
                          .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                          .collect(Collectors.toList());
    }

}
