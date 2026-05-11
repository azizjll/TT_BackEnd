package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.AbsenceRequest;
import com.example.TT_BackEnd.dto.SaisonnierAbsenceDTO;
import com.example.TT_BackEnd.dto.SaisonnierDTO;
import com.example.TT_BackEnd.service.SaisonnierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// SaisonnierController.java
@RestController
@RequestMapping("/api/saisonniers")
@CrossOrigin("*")   // adapte selon ton env
@RequiredArgsConstructor
public class SaisonnierController {

    private final SaisonnierService service;



    // Ajouter dans SaisonnierController.java

    // SaisonnierController.java
    @PostMapping("/absences-excessives")
    public ResponseEntity<List<SaisonnierAbsenceDTO>> getAbsencesExcessives(
            @RequestBody AbsenceRequest req) {
        return ResponseEntity.ok(
                service.findAbsencesExcessives(
                        req.getCampagneId(),
                        req.getRegionId(),
                        req.getSeuil(),
                        req.getAbsencesData()   // ← passer la map ici
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<SaisonnierDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaisonnierDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/by-campagne-region")
    public ResponseEntity<List<SaisonnierDTO>> getByCampagneAndRegion(
            @RequestParam Long campagneId,
            @RequestParam Long regionId) {
        return ResponseEntity.ok(service.findByCampagneAndRegion(campagneId, regionId));
    }

    @GetMapping("/by-campagne-structure")
    public ResponseEntity<List<SaisonnierDTO>> getByCampagneAndStructure(
            @RequestParam Long campagneId,
            @RequestParam Long structureId) {
        return ResponseEntity.ok(service.findByCampagneAndStructure(campagneId, structureId));
    }
}