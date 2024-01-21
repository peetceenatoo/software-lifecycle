package com.polimi.PPP.CodeKataBattle.Model;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class BadgeAssignment {

    @EmbeddedId
    private BadgeAssignmentKey id;

    private Timestamp date;

}
