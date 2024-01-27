package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleStateException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidTokenException;
import com.polimi.PPP.CodeKataBattle.Exceptions.UserNotSubscribedException;
import com.polimi.PPP.CodeKataBattle.Security.SubmissionAuthenticationToken;
import com.polimi.PPP.CodeKataBattle.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Objects;

@RestController
@RequestMapping("/api/battles")
public class BattleController extends AuthenticatedController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping("/{battleId}/commit")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> registerCommit(@RequestBody String commitHash, @PathVariable Long battleId) {
        SubmissionAuthenticationToken submissionAuth = this.getCommitToken();
        Long bId = submissionAuth.getBattleId();
        String repositoryUrl = submissionAuth.getRepositoryUrl();
        Long userId = submissionAuth.getUserId();

        if( !Objects.equals(bId, battleId) )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request.");
        try {
            submissionService.createSubmission(bId, userId, repositoryUrl, commitHash);
            return ResponseEntity.ok("Commit registered successfully.");
        } catch (InvalidBattleStateException | UserNotSubscribedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private SubmissionAuthenticationToken getCommitToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof SubmissionAuthenticationToken))
            throw new InvalidTokenException("Invalid Authentication Token");
        return (SubmissionAuthenticationToken) auth;
    }

}
