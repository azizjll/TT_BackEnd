package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.repository.StructureRepository;
import com.example.TT_BackEnd.service.AffectationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/affectations")
public class AffectationController {

    @Autowired
    private AffectationService affectationService;

    @Autowired
    private StructureRepository structureRepository;

    @PostMapping("/assign")
    public ResponseEntity<?> affecter(
            @RequestParam Long saisonnierId,
            @RequestParam Long structureId,
            @RequestParam Long campagneId) {

        affectationService.affecterSaisonnier(saisonnierId, structureId, campagneId);

        return ResponseEntity.ok(
                Map.of("message", "Affectation réalisée avec succès")
        );
    }

    @GetMapping("/region/{regionId}")
    public List<Structure> getByRegion(@PathVariable Long regionId){
        return structureRepository.findByRegionId(regionId);
    }
}