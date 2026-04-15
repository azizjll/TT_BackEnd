package com.example.TT_BackEnd.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private Integer cin;

    private String telephone;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    private String password;

    private Boolean enabled = false; // validation par admin ou verification email

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region; // obligatoire pour RH, facultatif pour admin ou saisonnier

    // --- Tokens ---
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private VerificationToken verificationToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private PasswordResetToken passwordResetToken;
}
