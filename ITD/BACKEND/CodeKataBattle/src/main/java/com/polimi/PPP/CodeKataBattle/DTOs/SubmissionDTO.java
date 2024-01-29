package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.SubmissionStateEnum;
import com.polimi.PPP.CodeKataBattle.Model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {

    private Long id;

    private Timestamp timestamp;

    private SubmissionStateEnum state;

    private String repositoryUrl;

    private String commitHash;

    private UserDTO user;

    private BattleDTO battle;
}
