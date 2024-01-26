// ... [existing imports]

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository; // Assuming this exists

    public void createSubmission(Long battleId, Long userId, String repositoryUrl, String commitHash) {
        Submission submission = new Submission();
        submission.setTimestamp(new Timestamp(System.currentTimeMillis()));
        submission.setProcessed(false);
        submission.setRepositoryUrl(repositoryUrl);
        submission.setCommitHash(commitHash);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Battle battle = battleRepository.findById(battleId).orElseThrow(() -> new EntityNotFoundException("Battle not found"));

        submission.setUser(user);
        submission.setBattle(battle);

        submissionRepository.save(submission);
    }

    // ... [other methods]
}
