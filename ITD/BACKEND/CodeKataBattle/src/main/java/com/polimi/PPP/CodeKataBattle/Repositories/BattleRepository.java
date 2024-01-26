package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    List<Battle> findByTournamentId(Long tournamentId);

    @Query("SELECT b FROM Battle b JOIN b.battleSubscriptions bs WHERE b.tournament.id = :tournamentId AND bs.user.id = :userId")
    List<Battle> findBattlesByTournamentIdAndUserId(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);
}
