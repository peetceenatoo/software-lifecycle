package com.polimi.PPP.CodeKataBattle.Security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class UserIdAuthenticationToken extends UsernamePasswordAuthenticationToken
{
    private final Long userId;

    public UserIdAuthenticationToken(Long userId) {
        super(null, Collections.emptyList());
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

}
