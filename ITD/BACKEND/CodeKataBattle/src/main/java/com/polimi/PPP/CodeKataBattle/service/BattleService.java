package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Model.BattleScore;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleScoreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BattleService {

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private BattleScoreRepository battleScoreRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<BattleDTO> getBattlesByTournamentId(Long tournamentId) {
        return battleRepository.findByTournamentId(tournamentId).stream()
                               .map(battle -> modelMapper.map(battle, BattleDTO.class))
                               .collect(Collectors.toList());
    }

    public List<BattleDTO> getEnrolledBattlesByTournamentId(Long tournamentId, Long userId) {
        return battleRepository.findBattlesByTournamentIdAndUserId(tournamentId, userId).stream()
                .map(battle -> modelMapper.map(battle, BattleDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<String> correctScore(Long submissionId, int correction) {
        Optional<BattleScore> battleScoreOpt = battleScoreRepository.findBySubmissionId(submissionId);

        if (battleScoreOpt.isPresent()) {
            BattleScore battleScore = battleScoreOpt.get();
            battleScore.setManualCorrection(correction);
            battleScoreRepository.save(battleScore);
            return Optional.of("Success");
        } else {
            throw new InvalidArgumentException("Invalid submission id");
        }
    }
}
