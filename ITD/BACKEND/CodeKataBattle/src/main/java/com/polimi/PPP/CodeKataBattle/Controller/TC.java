package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/endpoint")
public class TC extends AuthenticatedController{

    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnunm).ROLE_EDUCATOR)")
    @GetMapping("/requestPath")
    public String me() {
        UserDTO user = this.getAuthenticatedUser();
        return "Hello World!";
    }
}
