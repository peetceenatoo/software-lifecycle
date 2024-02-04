package com.polimi.PPP.CodeKataBattle.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BestBattleScoreDTO {
    private Long userId;
    private Long battleId;
    private Integer bestScore;

    // Getters and setters
}
