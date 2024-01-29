package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.DTOs.GroupSubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Model.Submission;
import com.polimi.PPP.CodeKataBattle.Model.SubmissionStateEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {


    List<Submission> findByBattleIdAndUserId(Long battleId, Long userId);

    @Query("SELECT s FROM Submission s JOIN BattleSubscription bs ON s.user.id = bs.user.id WHERE bs.groupId = :groupId AND s.battle.id = :battleId")
    List<Submission> findSubmissionsByGroupIdAndBattleId(Long groupId, Long battleId);


    @Query("SELECT new com.polimi.PPP.CodeKataBattle.DTOs.GroupSubmissionDTO(s.id, s.timestamp, s.repositoryUrl, s.commitHash, bs.automaticScore, bs.manualCorrection, bs.logScoring, sub.groupId) " +
            "FROM Submission s JOIN s.user u JOIN BattleSubscription sub ON u.id = sub.user.id " +
            "JOIN BattleScore bs ON s.id = bs.submission.id " +
            "WHERE s.battle.id = :battleId AND sub.groupId = " +
            "(SELECT sub2.groupId FROM BattleSubscription sub2 WHERE sub2.user.id = :userId AND sub2.battle.id = :battleId)")
    List<GroupSubmissionDTO> findSubmissionsByUserGroupInBattle(Long userId, Long battleId);

    @Query("SELECT new com.polimi.PPP.CodeKataBattle.DTOs.GroupSubmissionDTO(s.id, s.timestamp, s.repositoryUrl, s.commitHash, bs.automaticScore, bs.manualCorrection, bs.logScoring, sub.groupId) " +
            "FROM Submission s JOIN s.user u JOIN BattleSubscription sub ON u.id = sub.user.id " +
            "JOIN BattleScore bs ON s.id = bs.submission.id " +
            "WHERE s.battle.id = :battleId")
    List<GroupSubmissionDTO> findAllSubmissionsWithScoresByBattleId(Long battleId);
    // Repository methods as needed

    List<Submission> findAllByState(SubmissionStateEnum state);
}

