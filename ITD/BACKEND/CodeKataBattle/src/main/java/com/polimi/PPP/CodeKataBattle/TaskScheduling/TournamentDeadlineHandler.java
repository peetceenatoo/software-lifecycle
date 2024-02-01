package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.Model.TournamentStateEnum;
import com.polimi.PPP.CodeKataBattle.service.TournamentService;

import java.util.Optional;

public class TournamentDeadlineHandler implements DeadlineHandler{

    private final TournamentService tournamentService;
    private final Long tournamentId;

    public TournamentDeadlineHandler(TournamentService tournamentService, Long tournamentId) {
        this.tournamentService = tournamentService;
        this.tournamentId = tournamentId;
    }

    @Override
    public void handleDeadline() {
        System.out.println("Tournament deadline reached");
        // Safety sleep
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }
        this.tournamentService.updateStateForTournament(tournamentId, TournamentStateEnum.ONGOING);
    }

    @Override
    public void run() {
        handleDeadline();
    }
}
