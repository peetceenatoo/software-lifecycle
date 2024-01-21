package com.polimi.PPP.CodeKataBattle.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    String email;
    @NotBlank(message = "Password is mandatory")
    String password;

}
