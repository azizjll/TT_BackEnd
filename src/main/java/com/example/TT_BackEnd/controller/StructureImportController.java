package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.StructureDTO;
import com.example.TT_BackEnd.entity.StructureType;
import com.example.TT_BackEnd.repository.StructureRepository;
import com.example.TT_BackEnd.service.ExcelReaderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/structures")
@CrossOrigin("*")
public class StructureImportController {

    private final ExcelReaderService excelReaderService;
    private final StructureRepository structureRepository;


    public StructureImportController(ExcelReaderService excelReaderService, StructureRepository structureRepository) {
        this.excelReaderService = excelReaderService;
        this.structureRepository = structureRepository;
    }



    // NOUVEAU — récupérer toutes les structures avec leur région
    @GetMapping
    public List<StructureDTO> getAllStructures() {
        return structureRepository.findAll().stream()
                .map(s -> new StructureDTO(
                        s.getId(),
                        s.getNom(),
                        s.getType() == StructureType.ESPACE_COMMERCIAL ? "EC" : "CT",
                        s.getRegion() != null ? s.getRegion().getNom() : "",
                        s.getAdresse() != null ? s.getAdresse() : "",
                        s.getAutorises(),
                        s.getRecrutes()
                ))
                .collect(Collectors.toList());
    }

    // NOUVEAU — upload Excel
    @PostMapping("/import-excel")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide");
        }
        try {
            excelReaderService.importStructures(file.getInputStream());
            return ResponseEntity.ok(Map.of("message", "Import réussi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
}

 /*@PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importExcel(@RequestParam("file") MultipartFile file) {

        try {

            excelReaderService.importStructures(file.getInputStream());

            return "Import réussi";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'import";
        }
    }*/