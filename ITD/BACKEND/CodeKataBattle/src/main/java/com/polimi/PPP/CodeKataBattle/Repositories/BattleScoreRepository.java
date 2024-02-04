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
            value = "SELECT bestScore, group_id FROM BestBattleScores WHERE battle_fk = :battleId", nativeQuery = true
    )
    List<Object[]> calculateStudentRankingForBattle(Long battleId);

}

