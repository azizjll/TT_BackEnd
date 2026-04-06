package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.StructureDTO;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.repository.StructureRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/structures")
@CrossOrigin("*")
public class StructureController {

    private final StructureRepository structureRepository;

    public StructureController(StructureRepository structureRepository) {
        this.structureRepository = structureRepository;
    }

    @GetMapping("/region/{regionId}")
    public List<StructureDTO> getStructuresByRegion(@PathVariable Long regionId) {
        return structureRepository.findByRegionId(regionId)
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


}