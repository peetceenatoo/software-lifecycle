package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.BattleStudentDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.ScoreCorrectionDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidBattleStateException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidTokenException;
import com.polimi.PPP.CodeKataBattle.Exceptions.UserNotSubscribedException;
import com.polimi.PPP.CodeKataBattle.Model.Battle;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import com.polimi.PPP.CodeKataBattle.Security.SubmissionAuthenticationToken;
import com.polimi.PPP.CodeKataBattle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/battles")
public class BattleController extends AuthenticatedController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private BattleService battleService;

    @PostMapping("/{battleId}/commit")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> registerCommit(@RequestBody String commitHash, @PathVariable Long battleId) {
        SubmissionAuthenticationToken submissionAuth = this.getCommitToken();
        Long bId = submissionAuth.getBattleId();
        String repositoryUrl = submissionAuth.getRepositoryUrl();
        Long userId = submissionAuth.getUserId();

        if( !Objects.equals(bId, battleId) )
            throw new InvalidTokenException("Invalid token for the battle.");

        submissionService.createSubmission(bId, userId, repositoryUrl, commitHash);
        return ResponseEntity.ok("Commit registered successfully.");
    }

    @PostMapping("/correctScore")
    public ResponseEntity<?> correctScore(@RequestBody ScoreCorrectionDTO correctionDTO) {
        Optional<String> result = battleService.correctScore(correctionDTO.getSubmissionId(), correctionDTO.getCorrection());
        return ResponseEntity.ok(result);
    }

    private SubmissionAuthenticationToken getCommitToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof SubmissionAuthenticationToken))
            throw new InvalidTokenException("Invalid Authentication Token");
        return (SubmissionAuthenticationToken) auth;
    }

    @GetMapping("/{battleId}")
    public ResponseEntity<?> getBattle(@PathVariable Long battleId) {
        UserDTO user = this.getAuthenticatedUser();
        if (user.getRole().getName() == RoleEnum.ROLE_EDUCATOR) {
            Optional<BattleDTO> battle = battleService.getBattleByIdEducator(battleId, user.getId());
            if (battle.isPresent()) {
                return ResponseEntity.ok(battle.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Battle not found");
            }
        }else {
            Optional<BattleStudentDTO> battle = battleService.getBattleByIdStudent(battleId, user.getId());
            if (battle.isPresent()) {
                return ResponseEntity.ok(battle.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Battle not found");
            }
        }

    }

}
