package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.Utilities.GitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.IGitHubAPI;
import com.polimi.PPP.CodeKataBattle.Utilities.TimezoneUtil;
import com.polimi.PPP.CodeKataBattle.service.BattleInviteService;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;


import java.time.ZonedDateTime;

public class BattleSubscriptionDeadlineHandler implements DeadlineHandler {
    private final BattleService battleService;
    private final BattleInviteService battleInviteService;
    private final Long battleId;
    private final TaskScheduler taskScheduler;
    private final IGitHubAPI gitHubAPI;

    public BattleSubscriptionDeadlineHandler(BattleService battleService, BattleInviteService battleInviteService, Long battleId, TaskScheduler taskScheduler, IGitHubAPI gitHubAPI) {
        this.battleService = battleService;
        this.battleInviteService = battleInviteService;
        this.battleId = battleId;
        this.taskScheduler = taskScheduler;
        this.gitHubAPI = gitHubAPI;
    }
    @Override
    public void handleDeadline() {

        //Reject all not accepted invites
        battleInviteService.changeBattleInvitesState(battleId, BattleInviteStateEnum.PENDING, BattleInviteStateEnum.REJECTED);
        //Reject invites for groups that have not reached the minimum number of participants
        battleInviteService.rejectGroupsNotReachedMinimum(battleId);

        // Move battle to Ongoing stage
        battleService.changeBattleState(battleId, BattleStateEnum.ONGOING);

        BattleDTO battle = battleService.getBattleById(battleId);

        gitHubAPI.changeRepositoryVisibility(battle.getRepositoryLink(), false);

        // Schedule a task to run at the submission deadline
        ZonedDateTime submissionDeadline = TimezoneUtil.convertUtcToLocalTime(battle.getSubmissionDeadline());
        taskScheduler.schedule(new BattleSubmissionDeadlineHandler(battleService, battleId), submissionDeadline.toInstant());
    }

    @Override
    public void run() {
        handleDeadline();
    }
}
