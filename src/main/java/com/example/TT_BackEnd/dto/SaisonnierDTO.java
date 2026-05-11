package com.example.TT_BackEnd.dto;

import com.example.TT_BackEnd.entity.Saisonnier;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SaisonnierDTO {
    private Long id;
    private String nom;
    private String prenom;
    private Integer cin;
    private String rib;
    private String telephone;   // ← ajouter

    public static SaisonnierDTO from(Saisonnier s) {
        SaisonnierDTO dto = new SaisonnierDTO();
        dto.setId(s.getId());
        dto.setNom(s.getNom());
        dto.setPrenom(s.getPrenom());
        dto.setCin(s.getCin());
        dto.setRib(s.getRib());
        dto.setTelephone(s.getTelephone());   // ← ajouter
        return dto;
    }
}