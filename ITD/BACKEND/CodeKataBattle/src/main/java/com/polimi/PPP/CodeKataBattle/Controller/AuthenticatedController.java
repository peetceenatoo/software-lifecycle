package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUserIdException;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AuthenticatedController {

    @Autowired
    protected UserService userService;

    protected UserDTO getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO user = null;
        if(auth instanceof UsernamePasswordAuthenticationToken){
            Long userId = Long.parseLong(((UsernamePasswordAuthenticationToken) auth).getPrincipal().toString());
            user = userService.findById(userId);
        }
        if (user == null) {
            throw new InvalidUserIdException("User not found");
        }
        return user;
    }

}
