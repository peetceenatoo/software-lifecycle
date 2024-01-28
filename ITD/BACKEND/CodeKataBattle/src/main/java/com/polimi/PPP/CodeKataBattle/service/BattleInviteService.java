package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import com.polimi.PPP.CodeKataBattle.Repositories.BattleInviteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BattleInviteService {

    @Autowired
    private BattleInviteRepository battleInviteRepository;
    @Autowired
    private ModelMapper modelMapper;


    public void changeBattleInvitesState(Long battleInviteId, BattleInviteStateEnum oldState, BattleInviteStateEnum newState){
        battleInviteRepository.updateStateForBattle(battleInviteId, oldState, newState);
    }
}
