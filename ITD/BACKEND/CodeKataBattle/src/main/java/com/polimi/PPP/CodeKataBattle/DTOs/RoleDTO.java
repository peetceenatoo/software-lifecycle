package com.polimi.PPP.CodeKataBattle.DTOs;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {


    private Long id;
    private String name;

}
