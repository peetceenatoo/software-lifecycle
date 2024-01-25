package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnunm;
import com.polimi.PPP.CodeKataBattle.Model.TournamentStateEnum;
import com.polimi.PPP.CodeKataBattle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private UserService userService;

    @Autowired
    private BattleService battleService;

    @GetMapping("/{tournamentId}")
    public ResponseEntity<?> getTournament(@PathVariable Long tournamentId) {
        TournamentDTO tournament = tournamentService.getTournamentById(tournamentId);
        return ResponseEntity.ok(tournament);
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getTournaments(@RequestParam(required = false) TournamentStateEnum state) {
        List<TournamentDTO> tournaments = tournamentService.getTournaments(state);
        return ResponseEntity.ok(tournaments);
    }


    @GetMapping("/created")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> getCreatedTournaments() {
        UserDTO user = this.getAuthenticatedUser();
        List<TournamentDTO> createdTournaments = tournamentService.getCreatedTournaments(user.getId());
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
    public ResponseEntity<?> createTournament(@RequestBody TournamentCreationDTO tournamentCreationDTO) {

        UserDTO authenticatedUser = this.getAuthenticatedUser();
        tournamentCreationDTO.getEducatorsInvited().add(authenticatedUser.getId());
        TournamentDTO tournament = tournamentService.createTournament(tournamentCreationDTO);

        return ResponseEntity.ok(tournament);

    }

    /*@GetMapping("/{tournamentId}/ranking")
    public ResponseEntity<?> getRankingTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournamentRanking(tournamentId));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTournament(@RequestParam String keyword) {
        List<TournamentDTO> tournaments = tournamentService.searchTournamentsByKeyword(keyword);
        return ResponseEntity.ok(tournaments);
    }*/

    @PostMapping("/close")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> closeTournament(@RequestBody Long tournamentId) {

        tournamentService.closeTournament(tournamentId);
        return ResponseEntity.ok("Tournament closed successfully.");

    }

    @PostMapping("/enroll")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> enrollInTournament(@RequestBody Long tournamentId) {

        UserDTO authenticatedUser = this.getAuthenticatedUser();
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        tournamentService.enrollUserInTournament(tournamentId, authenticatedUser.getId());
        return ResponseEntity.ok("Enrollment successful.");

    }

}
