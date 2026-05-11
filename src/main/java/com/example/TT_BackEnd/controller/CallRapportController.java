package com.example.TT_BackEnd.controller;

// controller/CallRapportController.java

import com.example.TT_BackEnd.dto.CallRapportDTO;
import com.example.TT_BackEnd.entity.CallRapport;
import com.example.TT_BackEnd.repository.CallRapportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appels")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CallRapportController {

    private final CallRapportRepository rapportRepo;

    // Flask envoie le rapport ici après chaque appel
    @PostMapping("/rapport")
    public ResponseEntity<?> saveRapport(@RequestBody CallRapportDTO dto) {
        CallRapport rapport = new CallRapport();
        rapport.setSaisonnierTelephone(dto.getTelephone());
        rapport.setCallId(dto.getCallId());
        rapport.setStatut(dto.getStatut());
        rapport.setResume(dto.getResume());
        rapport.setDateAppel(LocalDateTime.now());
        rapportRepo.save(rapport);
        return ResponseEntity.ok(Map.of("saved", true));
    }

    // Angular peut afficher l'historique des appels d'un saisonnier
    @GetMapping("/historique/{telephone}")
    public ResponseEntity<List<CallRapport>> getHistorique(
            @PathVariable String telephone) {
        return ResponseEntity.ok(
                rapportRepo.findBySaisonnierTelephone(telephone)
        );
    }

    // Liste complète pour un tableau de bord admin
    @GetMapping
    public ResponseEntity<List<CallRapport>> getAll() {
        return ResponseEntity.ok(rapportRepo.findAll());
    }
}