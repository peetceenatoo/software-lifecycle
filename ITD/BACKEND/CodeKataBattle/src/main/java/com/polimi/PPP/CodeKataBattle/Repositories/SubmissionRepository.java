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

    List<Submission> findByBattleId(Long battleId);

    @Query("SELECT s FROM Submission s JOIN BattleSubscription bs ON s.user.id = bs.user.id WHERE bs.groupId = :groupId AND s.battle.id = :battleId")
    List<Submission> findSubmissionsByGroupIdAndBattleId(Long groupId, Long battleId);


    @Query(value = "SELECT s.id, s.timestamp, s.repository_url, s.commit_hash, bs.automatic_score, bs.manual_correction, bs.log_scoring, bsub.group_id\n" +
            "FROM submissions s " +
            "    JOIN se_project.battle_scores bs on s.id = bs.submission_fk " +
            "    JOIN battle_subscriptions bsub on (bsub.battle_id = s.battle_fk and bsub.user_id = s.user_fk) " +
            "where battle_id=:battleId and group_id = (SELECT group_id FROM battle_subscriptions " +
            "                                  WHERE battle_id = :battleId AND user_id = :userId)", nativeQuery = true)
    List<Object[]> findSubmissionsByUserGroupInBattle(Long userId, Long battleId);

    @Query(value = "SELECT s.id, s.timestamp, s.repository_url, s.commit_hash, bs.automatic_score, bs.manual_correction, bs.log_scoring, bsub.group_id\n" +
            "FROM submissions s " +
            "    JOIN se_project.battle_scores bs on s.id = bs.submission_fk " +
            "    JOIN battle_subscriptions bsub on (bsub.battle_id = s.battle_fk and bsub.user_id = s.user_fk) " +
            "where battle_id=:battleId and group_id IN (SELECT group_id FROM battle_subscriptions " +
            "                                  WHERE battle_id = :battleId)", nativeQuery = true)
    List<Object[]> findAllSubmissionsWithScoresByBattleId(Long battleId);
    // Repository methods as needed

    List<Submission> findAllByState(SubmissionStateEnum state);
}

