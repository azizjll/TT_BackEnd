package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.DemandeAutorisationDTO;
import com.example.TT_BackEnd.entity.Candidature;
import com.example.TT_BackEnd.service.CandidatureService;
import com.example.TT_BackEnd.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin("*")
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final JwtUtils jwtUtils;

    public CandidatureController(CandidatureService candidatureService, JwtUtils jwtUtils) {
        this.candidatureService = candidatureService;
        this.jwtUtils = jwtUtils;
    }

    // =========================
    // UTILITAIRE
    // =========================
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isEmpty()) ? ip.split(",")[0] : request.getRemoteAddr();
    }

    private String getCurrentUserEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return auth.getName();
    }

    // =========================
    // DEPOT (inchangé)
    // =========================
    @PostMapping(value = "/depot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> deposerCandidature(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String cin,
            @RequestParam String rib,
            @RequestParam String telephone,
            @RequestParam String email,
            @RequestParam String nomPrenomParent,
            @RequestParam String matriculeParent,
            @RequestParam String niveauEtude,
            @RequestParam String diplomeNom,
            @RequestParam String specialiteDiplome,
            @RequestParam String moisTravail,
            @RequestParam Long regionId,
            @RequestParam Long campagneId,
            @RequestParam Long structureId,
            @RequestParam("cinFile") MultipartFile cinFile,
            @RequestParam("diplome") MultipartFile diplome,
            @RequestParam("ribFile") MultipartFile ribFile,
            @RequestParam(defaultValue = "false") boolean demandeAdminAutorisee,
            @RequestParam(required = false, defaultValue = "") String messageDemandeAdmin,
            Authentication authentication
    ) {
        try {
            String rhEmail = authentication != null ? authentication.getName() : "";

            candidatureService.deposerCandidature(
                    nom.trim(), prenom.trim(), cin.trim(),
                    rib.trim(), telephone.trim(), email.trim(),
                    nomPrenomParent.trim(), matriculeParent.trim(),
                    niveauEtude.trim(), diplomeNom.trim(),
                    specialiteDiplome.trim(), moisTravail.trim(),
                    regionId, campagneId, structureId,
                    cinFile, diplome, ribFile,
                    demandeAdminAutorisee,
                    messageDemandeAdmin,
                    rhEmail
            );

            return ResponseEntity.ok().body(java.util.Map.of("message",
                    "Votre candidature a été envoyée avec succès. Un email contenant vos identifiants (email et mot de passe) vous a été envoyé. Veuillez consulter votre boîte mail pour accéder à votre compte."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("success", false, "message", e.getMessage())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    java.util.Map.of("success", false, "message", "Erreur serveur ❌")
            );
        }
    }

    // =========================
    // READ
    // =========================
    @GetMapping("/mes-candidatures")
    public ResponseEntity<?> getCandidaturesByRegion(
            @RequestParam Long regionId,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        var candidatures = candidatureService.getCandidaturesByRegion(regionId, email, getClientIp(request));
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/filtrer")
    public ResponseEntity<?> getCandidaturesByCampagneAndRegion(
            @RequestParam Long campagneId,
            @RequestParam Long regionId,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        var candidatures = candidatureService.getCandidaturesByCampagneAndRegion(campagneId, regionId, email, getClientIp(request));
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/filtrer/count")
    public ResponseEntity<?> countSaisonnierByCampagneAndRegion(
            @RequestParam Long campagneId,
            @RequestParam Long regionId,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        var candidatures = candidatureService.getCandidaturesByCampagneAndRegion(campagneId, regionId, email, getClientIp(request));
        long count = candidatures.stream()
                .map(c -> c.getSaisonnier().getId())
                .distinct()
                .count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCandidatures(HttpServletRequest request) {
        String email = getCurrentUserEmail();
        var candidatures = candidatureService.getAllCandidatures(email, getClientIp(request));
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/documents")
    public ResponseEntity<?> getDocumentsBySaisonnier(
            @RequestParam Long saisonnierId,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        var docs = candidatureService.getDocumentsBySaisonnier(saisonnierId, email, getClientIp(request));
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/saisonnier/{id}")
    public ResponseEntity<?> getSaisonnier(
            @PathVariable Long id,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        return ResponseEntity.ok(candidatureService.getSaisonnierById(id, email, getClientIp(request)));
    }

    @GetMapping("/mon-historique")
    public ResponseEntity<?> getMonHistorique(
            Authentication authentication,
            HttpServletRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        String email = authentication.getName();
        var candidatures = candidatureService.getHistoriqueCandidatures(email, getClientIp(request));
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/mes-documents")
    public ResponseEntity<?> getMesDocuments(
            Authentication authentication,
            HttpServletRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        String email = authentication.getName();
        var docs = candidatureService.getDocumentsByEmail(email, getClientIp(request));
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/mon-profil")
    public ResponseEntity<?> getMonProfil(
            Authentication authentication,
            HttpServletRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        String email = authentication.getName();
        return ResponseEntity.ok(candidatureService.getProfilByEmail(email, getClientIp(request)));
    }

    @GetMapping("/parent-by-matricule")
    public ResponseEntity<?> getParentByMatricule(
            @RequestParam String matricule,
            HttpServletRequest request) {

        try {
            String email = getCurrentUserEmail();
            return ResponseEntity.ok(candidatureService.getParentByMatricule(matricule, email, getClientIp(request)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/structure")
    public ResponseEntity<?> getStructureByCandidature(
            @PathVariable Long id,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        return ResponseEntity.ok(candidatureService.getStructureByCandidatureId(id, email, getClientIp(request)));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCandidature(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam Integer cin,
            @RequestParam String rib,
            @RequestParam String telephone,
            @RequestParam String email,
            @RequestParam Long regionId,
            @RequestParam(required = false) String moisTravail,
            @RequestParam String statut,
            @RequestParam(required = false) String commentaire,
            @RequestParam(required = false) Long structureId,
            @RequestParam(required = false, defaultValue = "") String nomPrenomParent,
            @RequestParam(required = false, defaultValue = "") String matriculeParent,
            @RequestParam(required = false, defaultValue = "") String niveauEtude,
            @RequestParam(required = false, defaultValue = "") String diplome,
            @RequestParam(required = false, defaultValue = "") String specialiteDiplome,
            HttpServletRequest request
    ) {
        String userEmail = getCurrentUserEmail();
        var candidature = candidatureService.updateCandidature(
                id, nom, prenom, cin, rib, telephone, email,
                regionId, moisTravail, statut, commentaire, structureId,
                nomPrenomParent, matriculeParent, niveauEtude, diplome, specialiteDiplome,
                userEmail, getClientIp(request)
        );
        return ResponseEntity.ok(candidature);
    }

    // =========================
    // LOGIQUE MÉTIER
    // =========================
    @PostMapping("/demande-autorisation")
    public ResponseEntity<?> demandeAutorisation(
            @RequestBody DemandeAutorisationDTO dto,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        candidatureService.envoyerDemandeJuilletAout(
                dto.getCandidatureId(),
                dto.getCommentaire(),
                email,
                getClientIp(request)
        );
        return ResponseEntity.ok(Map.of("message", "Email envoyé aux administrateurs"));
    }

    @PostMapping("/upload-parents")
    public ResponseEntity<?> uploadParentsExcel(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam Long campagneId,   // ← AJOUTER
            HttpServletRequest request) {
        try {
            candidatureService.uploadParentsExcel(
                    fichier,
                    campagneId,   // ← passer
                    getCurrentUserEmail(),
                    getClientIp(request)
            );
            return ResponseEntity.ok("Import réussi ✅");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/par-structure")
    public ResponseEntity<List<Candidature>> getCandidaturesParStructure(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtils.getUsernameFromToken(token);

        return ResponseEntity.ok(candidatureService.getCandidaturesParStructureResponsable(email));
    }
}