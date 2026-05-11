package com.example.TT_BackEnd.dto;



import lombok.Data;

@Data
public class CallRapportDTO {
    private String telephone;
    private String callId;
    private String statut;   // "APPELE" | "AVERTI" | "REJETE"
    private String resume;
}