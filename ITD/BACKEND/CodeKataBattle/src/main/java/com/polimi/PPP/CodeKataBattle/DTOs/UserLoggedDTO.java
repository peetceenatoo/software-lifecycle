package com.polimi.PPP.CodeKataBattle.DTOs;

import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedDTO {
    private String username;
    private String token;
    private RoleEnum role;

    // Add other fields as necessary, but avoid sensitive information like passwords
}
