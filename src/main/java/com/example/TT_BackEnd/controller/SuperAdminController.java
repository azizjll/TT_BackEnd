package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.repository.UtilisateurRepository;
import com.example.TT_BackEnd.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/superadmin")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SuperAdminController {

    private final ExcelImportService excelImportService;

    private final UtilisateurRepository utilisateurRepository;

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
                "message",  "Import terminé",
                "créés",    result.created(),
                "ignorés",  result.skipped(),
                "erreurs",  result.errors()
        ));
    }

    @GetMapping("/users")

    public ResponseEntity<?> getAllUsers() {

        return ResponseEntity.ok(
                utilisateurRepository.findAll()
        );
    }
}