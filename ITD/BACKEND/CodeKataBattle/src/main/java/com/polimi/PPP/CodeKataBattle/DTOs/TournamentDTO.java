package com.polimi.PPP.CodeKataBattle.DTOs;
import com.polimi.PPP.CodeKataBattle.Model.TournamentStateEnum;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TournamentDTO {
    private Long id;
    private String name;
    private LocalDateTime deadline;
    private TournamentStateEnum state;
}