package com.polimi.PPP.CodeKataBattle.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BattleRankingGroupDTO {
    private Long groupId;
    private int highestScore;

    // Manually define the constructor
    public BattleRankingGroupDTO(Long groupId, int highestScore) {
        this.groupId = groupId;
        this.highestScore = highestScore;
    }
}
