package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class BattleInviteKey implements Serializable {
    @Column(name = "battle_id")
    private Long battleId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "invited_user_id")
    private Long invitedUserId;


}
