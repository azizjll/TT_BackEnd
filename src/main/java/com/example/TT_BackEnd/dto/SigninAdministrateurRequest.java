package com.example.TT_BackEnd.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninAdministrateurRequest {

    private Integer matricule;

    private String password;
}