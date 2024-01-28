package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.service.BattleInviteService;
import com.polimi.PPP.CodeKataBattle.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class BattleSubscriptionDeadlineHandler implements DeadlineHandler {
    private final BattleService battleService;
    private final BattleInviteService battleInviteService;
    private final Long battleId;

    private final TaskScheduler taskScheduler;

    public BattleSubscriptionDeadlineHandler(BattleService battleService, BattleInviteService battleInviteService, Long battleId, TaskScheduler taskScheduler) {
        this.battleService = battleService;
        this.battleInviteService = battleInviteService;
        this.battleId = battleId;
        this.taskScheduler = taskScheduler;
    }
    @Override
    public void handleDeadline() {

        //Reject all not accepted invites
        battleInviteService.changeBattleInvitesState(battleId, BattleInviteStateEnum.PENDING, BattleInviteStateEnum.REJECTED);

        // Move battle to Ongoing stage
        battleService.changeBattleState(battleId, BattleStateEnum.ONGOING);

        BattleDTO battle = battleService.getBattleById(battleId);

        // Schedule a task to run at the submission deadline
        taskScheduler.schedule(new BattleSubmissionDeadlineHandler(battleService, battleId), battle.getSubmissionDeadline().toInstant(ZoneOffset.UTC));
    }

    @Override
    public void run() {
        handleDeadline();
    }
}
