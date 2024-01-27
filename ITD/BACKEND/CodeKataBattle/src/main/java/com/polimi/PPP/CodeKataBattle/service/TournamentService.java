package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
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
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Boolean hasUserRightsOnTournament(Long userId, Long tournamentId){
        return tournamentRepository.hasUserRightsOnTournament(userId, tournamentId);
    }

    public TournamentDTO getTournamentById(Long tournamentId) {
        return modelMapper.map(tournamentRepository.findById(tournamentId).orElse(null), TournamentDTO.class);
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

    @Transactional
    public TournamentDTO createTournament(TournamentCreationDTO tournamentDTO) {
        // Input validation done in the controller
        Tournament tournament = new Tournament();
        modelMapper.map(tournamentDTO, tournament);
        tournament.setState(TournamentStateEnum.SUBSCRIPTION);
        Tournament savedTournament = tournamentRepository.save(tournament);

        // Handle association with educators
        for (Long id : tournamentDTO.getEducatorsInvited()) {
            User educator = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("EducatorId not valid"));
            educator.getTournaments().add(savedTournament);
            userRepository.save(educator);

            savedTournament.getUsers().add(educator);
        }

        // Save tournament
        savedTournament = tournamentRepository.save(savedTournament);

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

        if (tournament.getState() != TournamentStateEnum.SUBSCRIPTION) {
            throw new IllegalStateException("Tournament is not in subscription phase");
        }

        tournament.getUsers().add(user);
        user.getTournaments().add(tournament);

        userRepository.save(user);
        tournamentRepository.save(tournament);
    }

    public List<TournamentRankingDTO> getTournamentRanking(Long tournamentId){
        List<TournamentRankingDTO> ranking = tournamentRepository.calculateStudentRankingForTournament(tournamentId);
        return ranking;
    }

    public List<TournamentDTO> searchTournamentsByKeyword(String keyword) {

        Long id = -1L;
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
