// dto/SaisonnierAbsenceDTO.java
package com.example.TT_BackEnd.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaisonnierAbsenceDTO {
    private Long    id;
    private String  nom;
    private String  prenom;
    private String  telephone;
    private Integer absences;
    private Integer duree;
}