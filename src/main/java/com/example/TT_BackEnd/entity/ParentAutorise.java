package com.example.TT_BackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ParentAutorise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomPrenom;

    @Column(unique = true)
    private String matricule;

    private int autorises;   // nombre max d'utilisations (depuis Excel)
    private int utilise = 0; // 🔥 pour savoir si déjà utilisé
}