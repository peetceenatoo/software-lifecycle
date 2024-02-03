package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    List<Battle> findByTournamentId(Long tournamentId);

    List<Battle> findByTournamentIdAndState(Long tournamentId, BattleStateEnum state);

    @Query("SELECT b FROM Battle b WHERE (b.id = :id OR b.name LIKE %:keyword%) AND b.state = :state")
    List<Battle> findByKeywordOrIdAndState(String keyword, Long id, BattleStateEnum state);

    @Query("SELECT b FROM Battle b " +
            "JOIN b.tournament t " +
            "JOIN BattleSubscription bs ON bs.battle = b " +
            "WHERE t.id = :tournamentId AND bs.user.id = :userId")
    List<Battle> findBattlesByTournamentIdAndUserId(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    List<Battle> findByStateNotAndStateNot(BattleStateEnum state, BattleStateEnum state2);
    Boolean existsByTournamentIdAndName(Long tournamentId, String name);
}
