package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUserIdException;
import com.polimi.PPP.CodeKataBattle.Security.UserIdAuthenticationToken;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AuthenticatedController {

    @Autowired
    protected UserService userService;

    protected UserDTO getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO user = null;
        if (auth instanceof UserIdAuthenticationToken) {
            Long userId = ((UserIdAuthenticationToken) auth).getUserId();
            user = userService.findById(userId);
        }
        if (user == null) {
            throw new InvalidUserIdException("User not found");
        }
        return user;
    }

}
