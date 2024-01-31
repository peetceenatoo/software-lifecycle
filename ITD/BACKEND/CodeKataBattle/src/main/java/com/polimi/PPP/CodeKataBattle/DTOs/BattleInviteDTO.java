package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.BattleInviteStateEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class BattleInviteDTO {
    private Long id;
    private Long battleId;
    private Long invitedUserid;

    private Long userId;

    private BattleInviteStateEnum state;
}
