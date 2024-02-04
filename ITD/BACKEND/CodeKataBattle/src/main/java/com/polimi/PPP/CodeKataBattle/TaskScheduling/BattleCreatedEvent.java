package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import com.polimi.PPP.CodeKataBattle.DTOs.BattleDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BattleCreatedEvent extends ApplicationEvent {
    private final BattleDTO battle;
    public BattleCreatedEvent(Object source, BattleDTO battle) {
        super(source);
        this.battle = battle;
    }

}
