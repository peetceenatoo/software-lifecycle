package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BattleInvites")
public class BattleInvite {

        @EmbeddedId
        BattleInviteKey id;

        @ManyToOne
        @MapsId("battleId")
        @JoinColumn(name = "battle_id")
        private Battle battle;

        @ManyToOne
        @MapsId("userId")
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @MapsId("invitedUserId")
        @JoinColumn(name = "invited_user_id")
        private User InvitedUser;

        @Column(nullable = false)
        private boolean accepted;
}


