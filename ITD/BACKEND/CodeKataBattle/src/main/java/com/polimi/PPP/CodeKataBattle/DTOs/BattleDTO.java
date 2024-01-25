package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.BattleStateEnum;
import java.util.Date;

public class BattleDTO {
    private Long id;
    private String name;
    private String codeKataPath;
    private BattleStateEnum state;
    private Date subscriptionDeadline;
    private Date submissionDeadline;
    private int maxStudentsInGroup;
    private int minStudentsInGroup;
    private boolean manualScoringRequires;
    private String repositoryLink;
    // Getters and setters
}
