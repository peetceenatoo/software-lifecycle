package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidActionException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidRightsForActionException;
import com.polimi.PPP.CodeKataBattle.Model.TournamentStateEnum;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import com.polimi.PPP.CodeKataBattle.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController extends AuthenticatedController{

    // to get the request's UserDTO object, call this.getAuthenticatedUser();

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    @Qualifier("emailProvider")
    private NotificationProvider notificationProvider;

    @Autowired
    private BattleService battleService;

    @GetMapping("/{tournamentId}")
    public ResponseEntity<?> getTournament(@PathVariable Long tournamentId) {
        TournamentDTO tournament = tournamentService.getTournamentById(tournamentId);
        return ResponseEntity.ok(tournament);
    }

    @GetMapping
    public ResponseEntity<?> getTournaments() {
        UserDTO user = this.getAuthenticatedUser();
        List<TournamentDTO> tournaments = tournamentService.getTournaments(null);
        return ResponseEntity.ok(tournaments);
    }

    @GetMapping("/{state}")
    public ResponseEntity<List<TournamentDTO>> getTournaments(@PathVariable(required = false) TournamentStateEnum state) {
        UserDTO user = this.getAuthenticatedUser();
        List<TournamentDTO> tournaments = tournamentService.getTournaments(state);
        return ResponseEntity.ok(tournaments);
    }


    @GetMapping("/managed")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> getManagedTournaments() {
        UserDTO user = this.getAuthenticatedUser();
        List<TournamentDTO> createdTournaments = tournamentService.getManagedTournaments(user.getId());
        return ResponseEntity.ok(createdTournaments);
    }

    @GetMapping("/enrolled")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> getEnrolledTournaments() {
        UserDTO user = this.getAuthenticatedUser();
        List<TournamentDTO> enrolledTournaments = tournamentService.getEnrolledTournaments(user.getId());
        return ResponseEntity.ok(enrolledTournaments);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> createTournament(@Valid @RequestBody TournamentCreationDTO tournamentCreationDTO) {

        UserDTO authenticatedUser = this.getAuthenticatedUser();

        if (tournamentCreationDTO.getEducatorsInvited() == null)
            tournamentCreationDTO.setEducatorsInvited(new java.util.ArrayList<>());

        tournamentCreationDTO.getEducatorsInvited().add(authenticatedUser.getId());
        TournamentDTO tournament = tournamentService.createTournament(tournamentCreationDTO);

        return ResponseEntity.ok(tournament);
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<?> searchTournament(@PathVariable String keyword) {
        List<TournamentDTO> tournaments = tournamentService.searchTournamentsByKeyword(keyword);
        return ResponseEntity.ok(tournaments);
    }

    @GetMapping("/{tournamentId}/ranking")
    public ResponseEntity<?> getRankingTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournamentRanking(tournamentId));
    }

    @GetMapping("/{tournamentId}/battles")
    public ResponseEntity<?> getBattles(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(battleService.getBattlesByTournamentId(tournamentId));
    }

    @GetMapping("/{tournamentId}/battles/enrolled")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> getEnrolledBattles(@PathVariable Long tournamentId) {
        UserDTO authenticatedUser = this.getAuthenticatedUser();
        return ResponseEntity.ok(battleService.getEnrolledBattlesByTournamentId(tournamentId, authenticatedUser.getId()));
    }

    @PostMapping("/close")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> closeTournament(@RequestBody Long tournamentId) {

        //Preauthorize already checking if the user is an educator
        //Checking if he manages the tournament is enough
        UserDTO authenticatedUser = this.getAuthenticatedUser();
        if (!tournamentService.hasUserRightsOnTournament(authenticatedUser.getId(), tournamentId))
            throw new InvalidRightsForActionException("Not authorized to close this tournament.");

        tournamentService.closeTournament(tournamentId);
        return ResponseEntity.ok("Tournament closed successfully.");

    }

    @PostMapping("/enroll")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> enrollInTournament(@RequestBody Long tournamentId) {

        UserDTO authenticatedUser = this.getAuthenticatedUser();
        if (tournamentService.hasUserRightsOnTournament(authenticatedUser.getId(), tournamentId))
            throw new InvalidActionException("Already enrolled in this tournament.");

        tournamentService.enrollUserInTournament(tournamentId, authenticatedUser.getId());
        return ResponseEntity.ok("Enrollment successful.");

    }

    @PostMapping("/{tournamentId}/createBattle")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> createBattle(@PathVariable Long tournamentId, @RequestPart("battle") BattleCreationDTO battleDTO, @RequestPart("codeZip") MultipartFile codeZip, @RequestPart("testZip") MultipartFile testZip) {
        battleService.createBattle(tournamentId, battleDTO, codeZip, testZip);
        return ResponseEntity.ok("Enrollment successful.");
    }


}
