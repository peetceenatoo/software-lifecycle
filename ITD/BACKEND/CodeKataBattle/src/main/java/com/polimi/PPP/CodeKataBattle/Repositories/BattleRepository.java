package com.polimi.PPP.CodeKataBattle.Repository;

import com.polimi.PPP.CodeKataBattle.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    List<Battle> findByTournamentId(Long tournamentId);
}
