package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.TournamentDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TournamentCreatedEvent extends ApplicationEvent {

    private final TournamentDTO tournament;
    public TournamentCreatedEvent(Object source, TournamentDTO tournament) {
        super(source);
        this.tournament = tournament;
    }

}
