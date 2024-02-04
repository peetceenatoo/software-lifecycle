package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.ScoreCorrectionDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.SubmissionDTO;
import com.polimi.PPP.CodeKataBattle.Evaluators.EvaluatorProcess;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidArgumentException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidTokenException;
import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.*;
import com.polimi.PPP.CodeKataBattle.Model.*;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Security.SubmissionAuthenticationToken;
import com.polimi.PPP.CodeKataBattle.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/battles")
public class BattleController extends AuthenticatedController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private BattleService battleService;

    @Autowired
    private BattleInviteService battleInviteService;

    @Autowired
    private EvaluatorProcess evaluatorProcess;

    @Value("${frontendLink}")
    private String frontendUrl;

    @Autowired
    JwtHelper jwtHelper;

    @PostMapping("/commit")
    public ResponseEntity<?> registerCommit(@RequestBody @Valid CommitDTO commitDTO) {
        SubmissionAuthenticationToken submissionAuth = this.getCommitToken();
        Long bId = submissionAuth.getBattleId();
        Long userId = submissionAuth.getUserId();
        String repositoryUrl = commitDTO.getRepositoryUrl();
        String commitHash = commitDTO.getCommitHash();

        if(repositoryUrl.isEmpty() || commitHash.isEmpty())
            throw new InvalidArgumentException("Invalid arguments for the request.");

        SubmissionDTO submissionDTO = submissionService.createSubmission(bId, userId, repositoryUrl, commitHash);

        evaluatorProcess.processSubmission(submissionDTO);

        return ResponseEntity.ok("Commit registered successfully.");
    }

    @PostMapping("/correctScore")
    public ResponseEntity<?> correctScore(@RequestBody ScoreCorrectionDTO correctionDTO) {
        BattleScoreDTO result = submissionService.correctScore(correctionDTO.getSubmissionId(), correctionDTO.getCorrection());
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
                throw new InvalidArgumentException("Battle not found");
            }
        }else {
            Optional<BattleStudentDTO> battle = battleService.getBattleByIdStudent(battleId, user.getId());
            if (battle.isPresent()) {
                return ResponseEntity.ok(battle.get());
            } else {
                throw new InvalidArgumentException("Battle not found");
            }
        }

    }

    @PostMapping("/{battleId}/closeBattle")
    public ResponseEntity<?> closeBattle(@PathVariable Long battleId) {
        UserDTO user = this.getAuthenticatedUser();
        if (battleId == null) {
            throw new InvalidArgumentException("Battle id cannot be null");
        }
        return ResponseEntity.ok(battleService.closeBattle(battleId, user));
    }

    @GetMapping("/{battleId}/ranking")
    public ResponseEntity<?> getRankingBattle(@PathVariable Long battleId) {
        UserDTO user = this.getAuthenticatedUser();
        if (battleId == null) {
            throw new InvalidArgumentException("Battle id cannot be null");
        }
        return ResponseEntity.ok(battleService.getBattleRanking(battleId, user));
    }

    @GetMapping("/{battleId}/submissions")
    public ResponseEntity<?> getSubmissions(@PathVariable Long battleId) {
        UserDTO user = this.getAuthenticatedUser();
        if (user.getRole().getName() == RoleEnum.ROLE_STUDENT) {
            Optional<BattleStudentDTO> battle = battleService.getBattleByIdStudent(battleId, user.getId());
            if (battle.isPresent()) {
                return ResponseEntity.ok(submissionService.getSubmissionsByUserGroupInBattle(user.getId(), battleId));
            } else {
                throw new InvalidArgumentException("Battle not found");
            }
        } else {
            Optional<BattleDTO> battle = battleService.getBattleByIdEducator(battleId, user.getId());
            if (battle.isPresent()) {
                return ResponseEntity.ok(submissionService.getAllSubmissionsWithScoresByBattle(battleId));
                //return ResponseEntity.ok(submissionService.getSubmissionsGroupedByGroupInBattle(battleId));
            } else {
                throw new InvalidArgumentException("Battle not found");
            }
        }
    }

    @PostMapping("/{battleId}/enroll")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> enrollToBattle(@PathVariable Long battleId, @RequestBody List<String> usernames) {
        UserDTO user = this.getAuthenticatedUser();
        BattleEnrollDTO battleEnrollDTO = new BattleEnrollDTO();
        battleEnrollDTO.setBattleId(battleId);
        battleEnrollDTO.setUserId(user.getId());
        battleEnrollDTO.setUsernames(usernames);
        battleInviteService.enrollAndInviteBattle(battleEnrollDTO);
        return ResponseEntity.ok("Enrolled successfully");
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> inviteToBattle(@RequestBody EnrollmentBattleDTO enrollmentBattleDTO) {
        UserDTO user = this.getAuthenticatedUser();
        BattleEnrollDTO battleEnrollDTO = new BattleEnrollDTO();
        battleEnrollDTO.setBattleId(enrollmentBattleDTO.getBattleId());
        battleEnrollDTO.setUserId(user.getId());
        battleEnrollDTO.setUsernames(enrollmentBattleDTO.getUsernames());
        battleInviteService.inviteUserToBattle(battleEnrollDTO);
        return ResponseEntity.ok("Invited successfully");
    }

    @GetMapping("/{battleId}/getGithubToken")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> getGithubToken(@PathVariable Long battleId) {
        UserDTO user = this.getAuthenticatedUser();
        Optional<BattleStudentDTO> battleStudentDTO = battleService.getBattleByIdStudent(battleId, user.getId());

        if(battleStudentDTO.isEmpty()){
            throw new InvalidArgumentException("Invalid battle for student");
        }

        BattleDTO battleDTO = battleStudentDTO.get().getBattle();

        //Check battle is ongoing
        if(battleDTO.getState() != BattleStateEnum.ONGOING){
            throw new InvalidBattleStateException("Cannot request token for battles that are not ongoing");
        }

        String userToken = jwtHelper.generateSubmissionToken(battleId, user.getId(), battleDTO.getSubmissionDeadline());

        return ResponseEntity.ok(new StudentGithubTokenDTO(userToken));
    }


    @GetMapping("/acceptInvitation/{token}")
    public RedirectView acceptInvitation(@PathVariable String token) {

        if(token == null || !jwtHelper.validateToken(token)){
            throw new InvalidTokenException("Invalid token provided");
        }

        String useCase = jwtHelper.extractUseCase(token);

        if(!useCase.equals(JWTTokenUseCase.BATTLE_INVITE.name())){
            throw new InvalidTokenException("Invalid token for the endpoint");
        }

        Long battleInviteId = jwtHelper.extractBattleInviteId(token);

        if(battleInviteId < 0) throw new InvalidArgumentException("Invalid invite id");

        battleInviteService.acceptBattleInvite(battleInviteId);

        //response to redirect to frontend homepage with react router to the home page

        // TODO: redirect to frontend not google :/)
        return new RedirectView(frontendUrl);
    }






}
