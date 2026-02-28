package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.service.CandidatureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin("*")
public class CandidatureController {

    private final CandidatureService candidatureService;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    @PostMapping(value = "/depot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> deposerCandidature(

            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam Integer cin,
            @RequestParam String rib,
            @RequestParam String telephone,
            @RequestParam String email,

            @RequestParam Long regionId,
            @RequestParam Long campagneId,

            @RequestPart MultipartFile cinFile,
            @RequestPart MultipartFile diplome,
            @RequestPart MultipartFile contrat

    ) throws Exception {

        candidatureService.deposerCandidature(
                nom, prenom, cin, rib, telephone, email,
                regionId, campagneId,
                cinFile, diplome, contrat
        );

        return ResponseEntity.ok("Candidature envoyée avec succès");
    }
}