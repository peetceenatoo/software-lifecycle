package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TournamentDTO getTournament(Long tournamentId) {
        return modelMapper.map(tournamentRepository.findById(tournamentId).orElse(null), TournamentDTO.class);
    }

    public List<TournamentDTO> getTournaments(String state) {
        if (state != null) {
            return tournamentRepository.findByState(TournamentStateEnum.valueOf(state))
                                       .stream()
                                       .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                                       .collect(Collectors.toList());
        }
        return tournamentRepository.findAll()
                                   .stream()
                                   .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
                                   .collect(Collectors.toList());
    }

    public List<TournamentDTO> getCreatedTournaments(Long educatorId) {
        return managerRepository.findByUserId(educatorId).stream()
            .map(manager -> getTournament(manager.getTournament().getId()))
            .collect(Collectors.toList());
    }

    public List<TournamentDTO> getEnrolledTournaments(Long studentId) {
        return tournamentRepository.findByUsers_Id(studentId).stream()
            .map(tournament -> modelMapper.map(tournament, TournamentDTO.class))
            .collect(Collectors.toList());
    }

    @Transactional
    public TournamentDTO createTournament(Long educatorId, String tournamentName, Date registrationDeadline, List<String> educatorsInvited) {
        // Validate inputs and business logic
        if (tournamentName == null || registrationDeadline == null || educatorsInvited.isEmpty()) {
            throw new IllegalArgumentException("Missing values");
        }

        Tournament tournament = new Tournament();
        tournament.setName(tournamentName);
        tournament.setDeadline(registrationDeadline);
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);

        Tournament savedTournament = tournamentRepository.save(tournament);

        // Handle association with educators
        for (String username : educatorsInvited) {
            User educator = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Values not valid"));
            Manager manager = new Manager();
            manager.setId(new ManagerKey(savedTournament.getId(), educator.getId()));
            manager.setTournament(savedTournament);
            manager.setUser(educator);
            managerRepository.save(manager);
        }

        return modelMapper.map(savedTournament, TournamentDTO.class);
    }

    public void closeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                                                    .orElseThrow(() -> new IllegalArgumentException("Tournament ID is wrong"));
    
        List<Battle> battles = battleRepository.findByTournamentId(tournamentId);
        boolean allBattlesEnded = battles.stream().allMatch(battle -> battle.getState() == BattleStateEnum.ENDED);
    
        if (!allBattlesEnded) {
            throw new IllegalStateException("There are still Battles ongoing or in consolidation stage.");
        }
    
        tournament.setState(TournamentStateEnum.ENDED);
        tournamentRepository.save(tournament);
    }

    @Transactional
    public void enrollUserInTournament(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        tournament.getUsers().add(user);
        tournamentRepository.save(tournament);
    }

    // Other methods corresponding to the TournamentController's endpoints
}
