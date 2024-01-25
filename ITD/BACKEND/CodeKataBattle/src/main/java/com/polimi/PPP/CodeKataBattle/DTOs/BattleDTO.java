package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class BattleDTO {
    private Long id;
    private String name;
    private String codeKataPath;
    private BattleStateEnum state;
    private LocalDateTime subscriptionDeadline;
    private LocalDateTime submissionDeadline;
    private int maxStudentsInGroup;
    private int minStudentsInGroup;
    private boolean manualScoringRequires;
    private String repositoryLink;
    // Getters and setters
}
