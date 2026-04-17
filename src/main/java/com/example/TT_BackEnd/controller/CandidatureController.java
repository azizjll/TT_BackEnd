package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.service.CandidatureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

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

            @RequestParam String nomPrenomParent,
            @RequestParam String matriculeParent,
            @RequestParam String niveauEtude,
            @RequestParam String diplomeNom,
            @RequestParam String specialiteDiplome,

            @RequestParam Long regionId,
            @RequestParam Long campagneId,
            @RequestParam Long structureId,

            @RequestParam("cinFile") MultipartFile cinFile,
            @RequestParam("diplome") MultipartFile diplome,
            @RequestParam("contrat") MultipartFile contrat
    ) {

        try {
            candidatureService.deposerCandidature(
                    nom, prenom, cin, rib, telephone, email,
                    nomPrenomParent, matriculeParent,
                    niveauEtude, diplomeNom, specialiteDiplome,
                    regionId, campagneId, structureId,
                    cinFile, diplome, contrat
            );

            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "message",
                            "Votre candidature a été envoyée avec succès. Un email contenant vos identifiants (email et mot de passe) vous a été envoyé. Veuillez consulter votre boîte mail pour accéder à votre compte."
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest().body(
                    java.util.Map.of("message", e.getMessage())
            );

        } catch (Exception e) {

            return ResponseEntity.status(500).body(
                    java.util.Map.of("message", "Erreur serveur")
            );
        }
    }

    @GetMapping("/mes-candidatures")
    public ResponseEntity<?> getCandidaturesByRegion(@RequestParam Long regionId) {
        var candidatures = candidatureService.getCandidaturesByRegion(regionId);
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/filtrer")
    public ResponseEntity<?> getCandidaturesByCampagneAndRegion(
            @RequestParam Long campagneId,
            @RequestParam Long regionId) {

        var candidatures = candidatureService.getCandidaturesByCampagneAndRegion(campagneId, regionId);
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/filtrer/count")
    public ResponseEntity<?> countSaisonnierByCampagneAndRegion(
            @RequestParam Long campagneId,
            @RequestParam Long regionId) {

        var candidatures = candidatureService.getCandidaturesByCampagneAndRegion(campagneId, regionId);
        long count = candidatures.stream()
                .map(c -> c.getSaisonnier().getId())
                .distinct()
                .count();

        return ResponseEntity.ok(count);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCandidatures() {

        var candidatures = candidatureService.getAllCandidatures();

        return ResponseEntity.ok(candidatures);
    }

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

            @RequestParam String statut,
            @RequestParam(required = false) String commentaire
    ) {

        var candidature = candidatureService.updateCandidature(
                id, nom, prenom, cin, rib, telephone, email,
                regionId, statut, commentaire
        );

        return ResponseEntity.ok(candidature);
    }

    @GetMapping("/documents")
    public ResponseEntity<?> getDocumentsBySaisonnier(@RequestParam Long saisonnierId) {
        var docs = candidatureService.getDocumentsBySaisonnier(saisonnierId);
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/saisonnier/{id}")
    public ResponseEntity<?> getSaisonnier(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.getSaisonnierById(id));
    }

    // Dans CandidatureController.java — ajouter :
    @GetMapping("/mon-historique")
    public ResponseEntity<?> getMonHistorique(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non authentifié");
        }

        String email = authentication.getName();

        var candidatures = candidatureService.getHistoriqueCandidatures(email);
        return ResponseEntity.ok(candidatures);
    }

}