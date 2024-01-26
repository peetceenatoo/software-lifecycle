@Repository
public interface BattleSubscriptionRepository extends JpaRepository<BattleSubscription, Long> {
    boolean existsByBattleIdAndUserId(Long battleId, Long userId);
}
