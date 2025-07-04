package com.workpilot.entity.ressources;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FakeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Ex: "Inconnu (JUNIOR_ENGINEER)"
    private String role;        // JUNIOR_ENGINEER, INTERMEDIATE_ENGINEER, SENIOR_ENGINEER, SENIOR_MANAGER (texte ou Enum)
    private String initial;     // Ex: "IJ" → généré automatiquement ou fourni
    private String note;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;

}
