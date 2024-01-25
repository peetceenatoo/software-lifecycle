package com.polimi.PPP.CodeKataBattle.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BattleInvites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"battle_id", "user_id", "invited_user_id"})
)
public class BattleInvite {

        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Id
        private Long id;

        @ManyToOne
        @JoinColumn(name = "battle_id", nullable = false)
        private Battle battle;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne
        @JoinColumn(name = "invited_user_id", nullable = true)
        private User InvitedUser;

        @Enumerated(EnumType.STRING)
        private BattleInviteStateEnum state;
}


