package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.BattleScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BattleScoreRepository extends JpaRepository<BattleScore, Long> {
    Optional<BattleScore> findBySubmissionId(Long submissionId);
}
