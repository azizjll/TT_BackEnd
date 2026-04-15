package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.CampagneRequestDTO;
import com.example.TT_BackEnd.entity.Campagne;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.service.CampagneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/campagnes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CampagneController {

    private final CampagneService campagneService;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne creerCampagne(
            @RequestBody CampagneRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {  // ← injecter l'utilisateur connecté
        return campagneService.creerCampagne(dto, userDetails.getUsername());
    }

    @PostMapping(value = "/avec-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne creerCampagneAvecExcel(
            @RequestPart("campagne") CampagneRequestDTO dto,
            @RequestPart("fichier") MultipartFile fichierExcel,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("=== UserDetails: " + userDetails); // ← vérifier
        System.out.println("=== Username: " + (userDetails != null ? userDetails.getUsername() : "NULL"));

        return campagneService.creerCampagneAvecExcel(dto, fichierExcel, userDetails.getUsername());
    }
    // =========================
    // READ
    // =========================
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH_REGIONAL')")
    public List<Campagne> getToutesCampagnes() {
        return campagneService.getToutesCampagnes();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH_REGIONAL')")
    public Campagne getCampagneParId(@PathVariable Long id) {
        return campagneService.getCampagneParId(id);
    }

    @GetMapping("/actives")
    @PreAuthorize("hasAnyRole('ADMIN','RH_REGIONAL')")
    public List<Campagne> getCampagnesActives() {
        return campagneService.getCampagnesActives();
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne mettreAJourCampagne(@PathVariable Long id, @RequestBody CampagneRequestDTO dto) {
        return campagneService.mettreAJourCampagne(id, dto);
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerCampagne(@PathVariable Long id) {
        campagneService.supprimerCampagne(id);
    }

    // =========================
    // LOGIQUE MÉTIER
    // =========================
    @PutMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne activerCampagne(@PathVariable Long id) {
        return campagneService.activerCampagne(id);
    }

    @PutMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne cloturerCampagne(@PathVariable Long id) {
        return campagneService.cloturerCampagne(id);
    }

    @GetMapping("/mes-campagnes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Campagne> getMesCampagnes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return campagneService.getCampagnesParCreateur(userDetails.getUsername());
    }

}