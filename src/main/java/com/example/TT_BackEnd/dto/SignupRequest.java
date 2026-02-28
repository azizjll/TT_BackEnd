package com.example.TT_BackEnd.dto;

import com.example.TT_BackEnd.entity.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String nom;
    private String prenom;
    private String email;
    private Integer cin;
    private String telephone;
    private String password;
    private RoleType role;
    private Long regionId; // facultatif selon rôle
}
