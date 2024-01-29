package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.GroupSubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleStateException;
import com.polimi.PPP.CodeKataBattle.Exceptions.UserNotSubscribedException;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleSubscriptionRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.SubmissionRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private BattleSubscriptionRepository battleSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    public void createSubmission(Long battleId, Long userId, String repositoryUrl, String commitHash) throws InvalidBattleStateException, UserNotSubscribedException {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new EntityNotFoundException("Battle not found"));

        if (battle.getState() != BattleStateEnum.ONGOING) {
            throw new InvalidBattleStateException("Battle is not in the ONGOING state.");
        }

        boolean isSubscribed = battleSubscriptionRepository.existsByBattleIdAndUserId(battleId, userId);
        if (!isSubscribed) {
            throw new UserNotSubscribedException("User is not subscribed to this battle.");
        }

        Submission submission = new Submission();
        submission.setTimestamp(new Timestamp(System.currentTimeMillis()));
        submission.setProcessed(false);
        submission.setRepositoryUrl(repositoryUrl);
        submission.setCommitHash(commitHash);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        submission.setUser(user);
        submission.setBattle(battle);

        submissionRepository.save(submission);
    }


    public List<GroupSubmissionDTO> getSubmissionsByUserGroupInBattle(Long userId, Long battleId) {
        return submissionRepository.findSubmissionsByUserGroupInBattle(userId, battleId);
    }
    public List<GroupSubmissionDTO> getAllSubmissionsWithScoresByBattle(Long battleId) {
        return submissionRepository.findAllSubmissionsWithScoresByBattleId(battleId);
    }

    public Map<Long, List<GroupSubmissionDTO>> getSubmissionsGroupedByGroupInBattle(Long battleId) {
        List<GroupSubmissionDTO> submissions = submissionRepository.findAllSubmissionsWithScoresByBattleId(battleId);

        // Grouping the submissions by groupId
        return submissions.stream().collect(Collectors.groupingBy(GroupSubmissionDTO::getGroupId));
    }


}
