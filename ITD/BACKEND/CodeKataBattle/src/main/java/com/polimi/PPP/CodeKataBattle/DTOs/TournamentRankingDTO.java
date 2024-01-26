package com.polimi.PPP.CodeKataBattle.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRankingDTO {
    private String username;
    private Long score;
}
