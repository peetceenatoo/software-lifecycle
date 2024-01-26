// ... [existing imports]

@RestController
@RequestMapping("/api/battles")
public class BattleController extends AuthenticatedController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping("/{battleId}/commit")
    @PreAuthorize("hasRole(T(com.polimi.PPP.CodeKataBattle.Model.RoleEnum).ROLE_STUDENT)")
    public ResponseEntity<?> registerCommit(@RequestBody String commitHash) {
        SubmissionAuthenticationToken submissionAuth = this.getCommitToken();
        Long battleId = submissionAuth.getBattleId();
        String repositoryUrl = submissionAuth.getRepositoryUrl();
        Long userId = submissionAuth.getUserId();
        submissionService.createSubmission(battleId, userId(), repositoryUrl, commitHash);
        return ResponseEntity.ok("Commit registered successfully.");
    }

    private SubmissionAuthenticationToken getCommitToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof SubmissionAuthenticationToken))
            throw new InvalidTokenException("Invalid Authentication Token");
        return (SubmissionAuthenticationToken) auth;
    }

    // ... [other methods and controller code]
}
