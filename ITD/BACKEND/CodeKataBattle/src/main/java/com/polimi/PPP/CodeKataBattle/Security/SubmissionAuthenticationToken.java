package com.polimi.PPP.CodeKataBattle.Security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;

public class SubmissionAuthenticationToken extends UsernamePasswordAuthenticationToken
{
    private final Long userId;
    private final Long battleId;
    private String repositoryUrl;

    public SubmissionAuthenticationToken(Long userId, Long battleId, String repositoryUrl) {
        super(null, Collections.emptyList());
        this.userId = userId;
        this.battleId = battleId;
        this.repositoryUrl = repositoryUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBattleId() {
        return battleId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

}
