package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.StructureDTO;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.repository.StructureRepository;
import com.example.TT_BackEnd.service.StructureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/structures")
@CrossOrigin("*")
public class StructureController {

    private final StructureRepository structureRepository;
    private final StructureService structureService; //


    public StructureController(StructureRepository structureRepository, StructureService structureService) {
        this.structureRepository = structureRepository;
        this.structureService = structureService;
    }

    @GetMapping("/region/{regionId}")
    public List<StructureDTO> getStructuresByRegion(
            @PathVariable Long regionId,
            @RequestParam(required = false) Long campagneId) {

        List<Structure> structures;

        if (campagneId != null) {
            // Filtrer par région ET campagne
            structures = structureRepository.findByRegionIdAndCampagneId(regionId, campagneId);
        } else {
            structures = structureRepository.findByRegionId(regionId);
        }

        return structures.stream()
                .map(s -> new StructureDTO(
                        s.getId(), s.getNom(), s.getType().name(),
                        s.getRegion().getNom(), s.getAdresse(),
                        s.getAutorises(), s.getRecrutes()
                ))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity   <?> updateStructure(
            @PathVariable Long id,
            @RequestBody StructureDTO dto) {

        Structure s = structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        s.setNom(dto.getNom());
        s.setAdresse(dto.getAdresse());
        s.setAutorises(dto.getAutorises());
        // Ne pas modifier recrutes depuis le DG — géré automatiquement

        structureRepository.save(s);
        return ResponseEntity.ok("Structure mise à jour");
    }

    @GetMapping("/campagne-active")
    public ResponseEntity<?> getStructuresCampagneActive() {
        try {
            List<StructureDTO> structures = structureService.getStructuresCampagneActive()
                    .stream()
                    .map(s -> new StructureDTO(
                            s.getId(),
                            s.getNom(),
                            s.getType().name(),
                            s.getRegion().getNom(),
                            s.getAdresse(),
                            s.getAutorises(),
                            s.getRecrutes()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(structures);

        } catch (Exception e) {
            System.out.println("=== Erreur structures : " + e.getMessage());
            return ResponseEntity.ok(List.of()); // [] au lieu de 500
        }
    }

    // ← endpoint public, pas besoin de JWT pour le formulaire d'inscription
    @GetMapping("/campagne-active/publique")
    public ResponseEntity<?> getStructuresCampagneActivePublique() {
        try {
            List<StructureDTO> structures = structureService
                    .getStructuresCampagneActivePublique()
                    .stream()
                    .map(s -> new StructureDTO(
                            s.getId(), s.getNom(), s.getType().name(),
                            s.getRegion().getNom(), s.getAdresse(),
                            s.getAutorises(), s.getRecrutes()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(structures);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }




}