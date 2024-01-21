package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class BadgeAssignmentKey implements Serializable {
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "user_id")
    private Long userId;
}
