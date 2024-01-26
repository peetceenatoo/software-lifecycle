// ... [existing imports]

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private BattleSubscriptionRepository battleSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    public void createSubmission(Long battleId, Long userId, String repositoryUrl, String commitHash) throws InvalidBattleStateException, UserNotSubscribedException {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new EntityNotFoundException("Battle not found"));

        if (battle.getState() != BattleStateEnum.ONGOING) {
            throw new InvalidBattleStateException("Battle is not in the ONGOING state.");
        }

        boolean isSubscribed = battleSubscriptionRepository.existsByBattleIdAndUserId(battleId, userId);
        if (!isSubscribed) {
            throw new UserNotSubscribedException("User is not subscribed to this battle.");
        }

        Submission submission = new Submission();
        submission.setTimestamp(new Timestamp(System.currentTimeMillis()));
        submission.setProcessed(false);
        submission.setRepositoryUrl(repositoryUrl);
        submission.setCommitHash(commitHash);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        submission.setUser(user);
        submission.setBattle(battle);

        submissionRepository.save(submission);
    }

    // ... [other methods]
}
