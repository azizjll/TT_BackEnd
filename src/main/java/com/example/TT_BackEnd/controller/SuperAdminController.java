package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.entity.Campagne;
import com.example.TT_BackEnd.entity.RoleType;
import com.example.TT_BackEnd.entity.StatutCampagne;
import com.example.TT_BackEnd.entity.Utilisateur;
import com.example.TT_BackEnd.repository.CampagneRepository;
import com.example.TT_BackEnd.repository.UtilisateurRepository;
import com.example.TT_BackEnd.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superadmin")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SuperAdminController {

    private final ExcelImportService excelImportService;

    private final UtilisateurRepository utilisateurRepository;
    private final CampagneRepository campagneRepository;

    /**
     * POST /superadmin/import-users
     *
     * Upload du fichier Excel contenant la liste des utilisateurs.
     * Accessible uniquement par le SUPERADMIN.
     *
     * Body : multipart/form-data — champ "file" = le fichier .xlsx
     *
     * Exemple curl :
     *   curl -X POST http://localhost:8080/superadmin/import-users \
     *        -H "Authorization: Bearer <jwt_token>" \
     *        -F "file=@liste_utilisateurs.xlsx"
     */
    @PostMapping("/import-users")

    public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le fichier est vide."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Format invalide. Veuillez envoyer un fichier Excel (.xlsx ou .xls)"));
        }

        ExcelImportService.ImportResult result = excelImportService.importFromExcel(file);

        return ResponseEntity.ok(Map.of(
                "message",   "Import terminé",
                "created",   result.created(),   // ← renommer en anglais pour cohérence
                "skipped",   result.skipped(),
                "deleted",   result.deleted(),   // ← nouveau
                "errors",    result.errors()
        ));
    }

    @PostMapping("/import-responsables-structure")
    public ResponseEntity<?> importResponsablesStructure(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le fichier est vide."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Format invalide. Veuillez envoyer un fichier Excel (.xlsx ou .xls)"));
        }

        try {
            ExcelImportService.ImportResult result =
                    excelImportService.importResponsablesStructure(file);

            return ResponseEntity.ok(Map.of(
                    "message", "Import Responsables Structure terminé",
                    "created", result.created(),
                    "skipped", result.skipped(),
                    "deleted", result.deleted(),
                    "errors",  result.errors()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Dans SuperAdminController — ajouter :

    @GetMapping("/users/responsables-structure-actifs")
    public ResponseEntity<?> getRsActifs() {
        List<Campagne> actives = campagneRepository.findByStatut(StatutCampagne.ACTIVE);
        if (actives.isEmpty()) {
            return ResponseEntity.ok(List.of()); // aucune campagne active → liste vide
        }
        Campagne campagneActive = actives.get(0);

        List<Utilisateur> rs = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() == RoleType.RESPONSABLE_STRUCTURE)
                .filter(u -> u.getCampagne() != null
                        && campagneActive.getId().equals(u.getCampagne().getId()))
                .toList();

        return ResponseEntity.ok(rs);
    }

    @GetMapping("/users")

    public ResponseEntity<?> getAllUsers() {

        return ResponseEntity.ok(
                utilisateurRepository.findAll()
        );
    }
}