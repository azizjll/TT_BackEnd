package com.example.TT_BackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AnalyseIA {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Candidature candidature;

    private Integer score;
    private String decision;       // ACCEPTE / REJETE / VERIFICATION_MANUELLE
    private String alertes;        // JSON string
    private String resume;

    @Column(name = "analyse_date")
    private LocalDateTime analyseDate;

    // getters/setters
}