package com.polimi.PPP.CodeKataBattle.Security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;

@Getter
public class SubmissionAuthenticationToken extends UsernamePasswordAuthenticationToken
{
    private final Long userId;
    private final Long battleId;

    public SubmissionAuthenticationToken(Long userId, Long battleId) {
        super(null, Collections.emptyList());
        this.userId = userId;
        this.battleId = battleId;
    }

}
