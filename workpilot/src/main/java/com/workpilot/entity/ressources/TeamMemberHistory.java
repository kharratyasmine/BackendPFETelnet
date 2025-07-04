package com.workpilot.entity.ressources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workpilot.entity.ressources.TeamMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_member_id", nullable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TeamMember teamMember;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @Column
    private String modifiedBy;
}