package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.CampagneRequestDTO;
import com.example.TT_BackEnd.entity.Campagne;
import com.example.TT_BackEnd.service.CampagneService;
import jakarta.servlet.http.HttpServletRequest;
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
    // UTILITAIRE
    // =========================
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isEmpty()) ? ip.split(",")[0] : request.getRemoteAddr();
    }

    // Remplacer @AuthenticationPrincipal UserDetails userDetails
    private String getCurrentUserEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return auth.getName(); // ← retourne l'email (le subject du JWT)
    }


    // =========================
    // CREATE
    // =========================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne creerCampagne(
            @RequestBody CampagneRequestDTO dto,

            HttpServletRequest request) {
        return campagneService.creerCampagne(dto,
                getCurrentUserEmail(), getClientIp(request));
    }

    @PostMapping(value = "/avec-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne creerCampagneAvecExcel(
            @RequestPart("campagne") CampagneRequestDTO dto,
            @RequestPart("fichier") MultipartFile fichierExcel,
            
            HttpServletRequest request) {
        return campagneService.creerCampagneAvecExcel(dto, fichierExcel,
                getCurrentUserEmail(), getClientIp(request));
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
    public Campagne mettreAJourCampagne(
            @PathVariable Long id,
            @RequestBody CampagneRequestDTO dto,
            
            HttpServletRequest request) {
        return campagneService.mettreAJourCampagne(id, dto,
                getCurrentUserEmail(), getClientIp(request));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerCampagne(
            @PathVariable Long id,
            
            HttpServletRequest request) {
        campagneService.supprimerCampagne(id,
                getCurrentUserEmail(), getClientIp(request));
    }

    // =========================
    // LOGIQUE MÉTIER
    // =========================
    @PutMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne activerCampagne(
            @PathVariable Long id,
            
            HttpServletRequest request) {
        return campagneService.activerCampagne(id,
                getCurrentUserEmail(), getClientIp(request));
    }

    @PutMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('ADMIN')")
    public Campagne cloturerCampagne(
            @PathVariable Long id,
            
            HttpServletRequest request) {
        return campagneService.cloturerCampagne(id,
                getCurrentUserEmail(), getClientIp(request));
    }

    @GetMapping("/mes-campagnes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Campagne> getMesCampagnes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return campagneService.getCampagnesParCreateur(userDetails.getUsername());
    }
}