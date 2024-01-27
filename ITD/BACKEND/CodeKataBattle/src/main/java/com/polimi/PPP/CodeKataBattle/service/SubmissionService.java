package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleStateException;
import com.polimi.PPP.CodeKataBattle.Exceptions.UserNotSubscribedException;
import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.Submission;
import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleSubscriptionRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.SubmissionRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

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


}
