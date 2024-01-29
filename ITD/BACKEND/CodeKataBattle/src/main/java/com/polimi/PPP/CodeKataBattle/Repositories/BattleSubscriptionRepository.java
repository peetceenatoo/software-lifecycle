package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import com.polimi.PPP.CodeKataBattle.Model.BattleSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleSubscriptionRepository extends JpaRepository<BattleSubscription, Long> {
    boolean existsByBattleIdAndUserId(Long battleId, Long userId);

    Optional<BattleSubscription> getBattleSubscriptionByBattleIdAndUserId(Long battleId, Long userId);


    @Query(value = "SELECT u.username " +
            "FROM BattleSubscription bs " +
            "JOIN bs.user u " +
            "WHERE bs.battle.id = :battleId AND bs.groupId = :groupId " +
            "ORDER BY u.username")
    List<String> findUsernamesByBattleId(Long battleId, Long groupId);

    Optional<BattleSubscription> findByUserIdAndBattleId(Long userId, Long battleId);

    @Query("SELECT MAX(bs.groupId) FROM BattleSubscription bs WHERE bs.battle.id = :battleId")
    Long findMaxGroupIdInBattle(Long battleId);

    long findGroupIdByBattleIdAndUserId(Long battleId, Long userId);
}
