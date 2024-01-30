package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleInviteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BattleInviteService {

    private final BattleInviteRepository battleInviteRepository;
    @Autowired
    public BattleInviteService(BattleInviteRepository battleInviteRepository) {
        this.battleInviteRepository = battleInviteRepository;
    }
    public void changeBattleInvitesState(Long battleInviteId, BattleInviteStateEnum oldState, BattleInviteStateEnum newState){

        if (oldState == null || newState == null) {
            throw new IllegalArgumentException("Old and new state must be not null");
        }

        if(oldState.equals(newState)){
            throw new IllegalArgumentException("Old and new state must be different");
        }

        if(oldState.equals(BattleInviteStateEnum.REJECTED)){
            throw new IllegalArgumentException("Old state cannot be REJECTED");
        }

        // Batch update using custom query
        this.battleInviteRepository.updateStateForBattle(battleInviteId, oldState, newState);
    }
}
