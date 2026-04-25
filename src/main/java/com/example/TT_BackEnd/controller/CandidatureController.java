package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.DemandeAutorisationDTO;
import com.example.TT_BackEnd.service.CandidatureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

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
            @RequestParam String moisTravail,

            @RequestParam Long regionId,
            @RequestParam Long campagneId,
            @RequestParam Long structureId,

            @RequestParam("cinFile") MultipartFile cinFile,
            @RequestParam("diplome") MultipartFile diplome,
            @RequestParam("contrat") MultipartFile contrat,
            @RequestParam("ribFile") MultipartFile ribFile
    ) {

        try {

            // 🔥 Trim pour éviter erreurs espaces
            candidatureService.deposerCandidature(
                    nom.trim(),
                    prenom.trim(),
                    cin,
                    rib.trim(),
                    telephone.trim(),
                    email.trim(),

                    nomPrenomParent.trim(),
                    matriculeParent.trim(),

                    niveauEtude.trim(),
                    diplomeNom.trim(),
                    specialiteDiplome.trim(),
                    moisTravail.trim(),

                    regionId,
                    campagneId,
                    structureId,

                    cinFile,
                    diplome,
                    contrat,
                    ribFile
            );

            return ResponseEntity.ok().body( java.util.Map.of( "message", "Votre candidature a été envoyée avec succès. Un email contenant vos identifiants (email et mot de passe) vous a été envoyé. Veuillez consulter votre boîte mail pour accéder à votre compte." ) );

        } catch (RuntimeException e) {

            // 🔥 erreurs métier (parent, quota, déjà candidat…)
            return ResponseEntity.badRequest().body(
                    java.util.Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            // 🔥 log backend (important)
            e.printStackTrace();

            return ResponseEntity.status(500).body(
                    java.util.Map.of(
                            "success", false,
                            "message", "Erreur serveur ❌"
                    )
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

            @RequestParam(required = false) String moisTravail,
            @RequestParam String statut,
            @RequestParam(required = false) String commentaire,
            @RequestParam(required = false) Long structureId
    ) {

        var candidature = candidatureService.updateCandidature(
                id, nom, prenom, cin, rib, telephone, email,
                regionId, moisTravail, statut, commentaire, structureId
        );

        return ResponseEntity.ok(candidature);
    }

    @PostMapping("/demande-autorisation")
    public ResponseEntity<?> demandeAutorisation(@RequestBody DemandeAutorisationDTO dto) {
        candidatureService.envoyerDemandeJuilletAout(
                dto.getCandidatureId(),
                dto.getCommentaire()
        );
        return ResponseEntity.ok(Map.of("message", "Email envoyé aux administrateurs"));
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

    @PostMapping("/upload-parents")
    public ResponseEntity<String> uploadParents(@RequestParam("file") MultipartFile file) {
        try {
            candidatureService.uploadParentsExcel(file);
            return ResponseEntity.ok("Upload réussi ✅");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur ❌ " + e.getMessage());
        }
    }

    @GetMapping("/mes-documents")
    public ResponseEntity<?> getMesDocuments(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        String email = authentication.getName();
        var docs = candidatureService.getDocumentsByEmail(email);
        return ResponseEntity.ok(docs);
    }


    @GetMapping("/mon-profil")
    public ResponseEntity<?> getMonProfil(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        String email = authentication.getName();
        return ResponseEntity.ok(candidatureService.getProfilByEmail(email));
    }

    @GetMapping("/{id}/structure")
    public ResponseEntity<?> getStructureByCandidature(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.getStructureByCandidatureId(id));
    }

}