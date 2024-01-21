package com.polimi.PPP.CodeKataBattle.Model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BattleSubscriptions")
public class BattleSubscription {

    @EmbeddedId
    BattleSubscriptionKey id;

    @ManyToOne
    @MapsId("battleId")
    @JoinColumn(name = "battle_id")
    private Battle battle;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean accepted;

}
