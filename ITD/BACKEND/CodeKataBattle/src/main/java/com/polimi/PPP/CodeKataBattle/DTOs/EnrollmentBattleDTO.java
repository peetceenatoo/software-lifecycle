package com.polimi.PPP.CodeKataBattle.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentBattleDTO {

    @NotBlank(message = "Battle id is mandatory")
    private Long battleId;

    @NotBlank(message = "Usernames are mandatory")
    private List<String> usernames;
}
