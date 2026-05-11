package com.example.TT_BackEnd.dto;

// dto/AbsenceRequest.java

import lombok.Data;

import java.util.Map;

@Data
public class AbsenceRequest {
    private Long campagneId;
    private Long regionId;
    private int  seuil;
    private Map<String, Integer> absencesData;

}