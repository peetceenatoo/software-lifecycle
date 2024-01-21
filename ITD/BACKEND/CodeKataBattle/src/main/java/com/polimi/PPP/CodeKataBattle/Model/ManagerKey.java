package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ManagerKey  implements Serializable {
    @Column(name = "tournament_id")
    private Long tournamentId;

    @Column(name = "user_id")
    private Long userId;
}

