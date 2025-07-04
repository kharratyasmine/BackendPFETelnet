package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workpilot.entity.ressources.Demande;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
@Builder
@Entity
@Table(name = "InvoicingDetail")
public class InvoicingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private LocalDate invoicingDate;
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    @JsonIgnore
    private Devis devis;


    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    @JsonIgnore
    private Demande demande;




}
