package com.polimi.PPP.CodeKataBattle.DTOs;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private String name;
    private String surname;
    private String email;
    private String username;
    private String linkBio;
    // Add other fields as necessary, but avoid sensitive information like passwords
}
