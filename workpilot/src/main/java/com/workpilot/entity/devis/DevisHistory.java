package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version;
    private String modificationDescription;
    private String action;
    private LocalDate date;
    private String name;

    @ManyToOne
    @JsonBackReference
    private Devis devis;

}
