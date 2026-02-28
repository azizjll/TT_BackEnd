package com.example.TT_BackEnd.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordRequest {
    private String token;
    private String newPassword;
}
