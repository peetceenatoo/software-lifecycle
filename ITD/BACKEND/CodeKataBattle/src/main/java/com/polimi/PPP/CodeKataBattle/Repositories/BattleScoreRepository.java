package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleRankingDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.BattleRankingGroupDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BattleScoreRepository extends JpaRepository<BattleScore, Long> {
    Optional<BattleScore> findBySubmissionId(Long submissionId);



    @Query(
            value = "SELECT new com.polimi.PPP.CodeKataBattle.DTOs.BattleRankingGroupDTO(bs.groupId, MAX(bScore.automaticScore + COALESCE(bScore.manualCorrection, 0))) " +
                    "FROM Submission sub " +
                    "JOIN sub.user u " +
                    "JOIN BattleSubscription bs ON bs.user.id = u.id AND bs.battle.id = sub.battle.id " +
                    "JOIN BattleScore bScore ON bScore.submission.id = sub.id " +
                    "WHERE sub.battle.id = :battleId " +
                    "GROUP BY bs.groupId " +
                    "ORDER BY MAX(bScore.automaticScore + COALESCE(bScore.manualCorrection, 0)) DESC"
    )
    List<BattleRankingGroupDTO> calculateStudentRankingForBattle(Long battleId);

}

