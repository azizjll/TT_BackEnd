package com.example.TT_BackEnd.dto;

import com.example.TT_BackEnd.entity.Saisonnier;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SaisonnierDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String cin;
    private String rib;
    private String statut;
    private String moisTravail;
    public Integer absences;


    // 🆕 statut passé en paramètre
    public static SaisonnierDTO from(Saisonnier s, String statut) {
        SaisonnierDTO dto = new SaisonnierDTO();
        dto.setId(s.getId());
        dto.setNom(s.getNom());
        dto.setPrenom(s.getPrenom());
        dto.setCin(String.valueOf(s.getCin()));
        dto.setRib(s.getRib());
        dto.setStatut(statut);           // ✅ paramètre, pas le champ
        dto.setMoisTravail(s.getMoisTravail());
        dto.absences    = s.getAbsences() != null ? s.getAbsences() : 0;
        return dto;
    }
}