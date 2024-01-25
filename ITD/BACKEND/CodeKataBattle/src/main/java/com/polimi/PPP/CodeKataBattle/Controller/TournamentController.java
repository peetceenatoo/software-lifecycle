package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController extends AuthenticatedController{

    // to get the request's UserDTO object, call this.getAuthenticatedUser();

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private UserService userService;

    @Autowired
    private BattleService battleService;

    @GetMapping("/{tournamentId}")
    public ResponseEntity<?> getTournament(@PathVariable int tournamentId) {
        try {
            TournamentDTO tournament = tournamentService.getTournamentById(tournamentId);
            if (tournament == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameters");
            }
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameters");
        }
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getTournaments(@RequestParam(required = false) String state) {
        List<TournamentDTO> tournaments = tournamentService.getAllTournaments(state);
        return ResponseEntity.ok(tournaments);
    }


    @GetMapping("/created")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<?> getCreatedTournaments() {
        UserDTO user = this.getAuthenticatedUser();
        try {
            List<TournamentDTO> createdTournaments = tournamentService.getCreatedTournaments(user.getId());
            return ResponseEntity.ok(createdTournaments);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/enrolled")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> getEnrolledTournaments() {
        UserDTO user = this.getAuthenticatedUser();
        try {
            List<TournamentDTO> enrolledTournaments = tournamentService.getEnrolledTournaments(user.getId());
            return ResponseEntity.ok(enrolledTournaments);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<String> createTournament(@RequestBody TournamentCreationDTO tournamentCreationDTO) {
        try {
            tournamentService.createTournament(
                tournamentCreationDTO.getEducatorId(),
                tournamentCreationDTO.getTournamentName(),
                tournamentCreationDTO.getRegistrationDeadline(),
                tournamentCreationDTO.getEducatorsInvited()
            );
            return ResponseEntity.ok("Tournament created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{tournamentId}/ranking")
    public ResponseEntity<List<UserRankingDTO>> getRankingTournament(@PathVariable int tournamentId) {
        // implementation for getting the ranking of a specific tournament
        return ResponseEntity.ok(tournamentService.getTournamentRanking(tournamentId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TournamentDTO>> searchTournament(@RequestParam String keyword) {
        List<TournamentDTO> tournaments = tournamentService.searchTournamentsByKeyword(keyword);
        return ResponseEntity.ok(tournaments);
    }

    @PostMapping("/close")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_EDUCATOR)")
    public ResponseEntity<String> closeTournament(@RequestBody Long tournamentId) {
        try {
            tournamentService.closeTournament(tournamentId);
            return ResponseEntity.ok("Tournament closed successfully.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/enroll")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<String> enrollInTournament(@RequestBody Long tournamentId) {
        try {
            UserDTO authenticatedUser = this.getAuthenticatedUser();
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }
            tournamentService.enrollUserInTournament(tournamentId, authenticatedUser.getId());
            return ResponseEntity.ok("Enrollment successful.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
