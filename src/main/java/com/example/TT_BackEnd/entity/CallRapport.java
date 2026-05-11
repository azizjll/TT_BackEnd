// entity/CallRapport.java
package com.example.TT_BackEnd.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_rapports")
@Data
@NoArgsConstructor
public class CallRapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String saisonnierTelephone;
    private String callId;
    private String statut;          // APPELE | AVERTI | REJETE

    @Column(columnDefinition = "TEXT")
    private String resume;

    private LocalDateTime dateAppel;
}