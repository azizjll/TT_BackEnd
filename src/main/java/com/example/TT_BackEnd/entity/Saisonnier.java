package com.example.TT_BackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Saisonnier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private Integer cin;

    private String rib;
    private String telephone;
    private String email;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;
}
