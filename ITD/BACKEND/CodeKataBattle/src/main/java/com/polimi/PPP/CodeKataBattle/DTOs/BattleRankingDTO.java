package com.polimi.PPP.CodeKataBattle.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BattleRankingDTO {
    private Long groupId;
    private List<String> usernames;
    private int highestScore;
}