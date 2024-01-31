package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import com.polimi.PPP.CodeKataBattle.service.BattleService;

public class BattleSubmissionDeadlineHandler implements DeadlineHandler{
    private final BattleService battleService;
    private final Long battleId;

    public BattleSubmissionDeadlineHandler(BattleService battleService, Long battleId) {
        this.battleService = battleService;
        this.battleId = battleId;
    }
    @Override
    public void handleDeadline() {
        // Move battle to consolidation stage
        battleService.changeBattleState(battleId, BattleStateEnum.CONSOLIDATION);
    }

    @Override
    public void run() {
        handleDeadline();
    }
}