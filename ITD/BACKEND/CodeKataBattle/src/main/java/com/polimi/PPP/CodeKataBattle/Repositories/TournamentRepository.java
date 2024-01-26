package com.polimi.PPP.CodeKataBattle.Repositories;

import com.polimi.PPP.CodeKataBattle.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByState(TournamentStateEnum state);

    List<Tournament> findByUsers_Id(Long userId); // For enrolled tournaments
    //Tournament findById(Long tournamentId);
    // Other custom methods if needed
}
