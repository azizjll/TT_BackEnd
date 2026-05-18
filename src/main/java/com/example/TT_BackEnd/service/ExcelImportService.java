package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.RoleType;
import com.example.TT_BackEnd.entity.Utilisateur;
import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.repository.UtilisateurRepository;
import com.example.TT_BackEnd.util.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final UtilisateurRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;


    /**
     * Structure du fichier Excel attendu (ligne 1 = en-têtes) :
     *
     * Colonne 0 : Matricule          (ex: 74151)
     * Colonne 1 : Nom & prénom       (ex: ABIDI Bassem)
     * Colonne 2 : Email              (ex: bassem.abidi@tunisietelecom.tn)
     * Colonne 3 : Rôle               (SuperAdmin / Administrateur RH / Responsable RH)
     * Colonne 4 : Direction d'affectation (ex: DIRECTION REGIONALE KAIROUAN)
     *
     * Mapping des rôles Excel → RoleType :
     *   "SuperAdmin"        → SUPERADMIN
     *   "Administrateur RH" → ADMIN
     *   "Responsable RH"    → RH_REGIONAL
     */
    public ImportResult importFromExcel(MultipartFile file) {
        int created = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Sauter la ligne d'en-têtes
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row row = rows.next();

                // Ignorer les lignes vides
                if (isRowEmpty(row)) continue;

                try {
                    // ---- Lecture des cellules ----
                    Integer matricule = readInt(row.getCell(0));
                    String nomPrenom  = readString(row.getCell(1));
                    String email      = readString(row.getCell(2));
                    String roleExcel  = readString(row.getCell(3));
                    String direction  = readString(row.getCell(4));

                    if (matricule == null || email == null || email.isBlank()) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " ignorée: matricule ou email manquant");
                        skipped++;
                        continue;
                    }

                    // ---- Vérifier si l'utilisateur existe déjà ----
                    if (userRepository.findByMatricule(matricule).isPresent() ||
                            userRepository.findByEmail(email).isPresent()) {
                        log.info("Utilisateur déjà existant, ignoré — matricule: {}", matricule);
                        skipped++;
                        continue;
                    }

                    // ---- Mapper le rôle ----
                    RoleType role = mapRole(roleExcel);
                    if (role == null) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " — rôle inconnu: " + roleExcel);
                        skipped++;
                        continue;
                    }

                    // ---- Décomposer nom & prénom ----
                    // Format attendu: "NOM Prenom" (premier mot = nom, reste = prénom)
                    String[] parts = splitNomPrenom(nomPrenom);
                    String nom    = parts[0];
                    String prenom = parts[1];

                    // ---- Créer l'utilisateur ----
                    Utilisateur user = new Utilisateur();
                    user.setNom(nom);
                    user.setPrenom(prenom);
                    user.setEmail(email.trim().toLowerCase());
                    user.setMatricule(matricule);   // matricule professionnel
                    // cin laissé null (non fourni dans le fichier Excel RH)
                    // Mot de passe = matricule
                    user.setPassword(passwordEncoder.encode(String.valueOf(matricule)));
                    user.setRole(role);
                    user.setEnabled(true); // Activé directement, pas de vérification email

                    // ---- Associer la région pour RH_REGIONAL ----
                    if (role == RoleType.RH_REGIONAL && direction != null && !direction.isBlank()) {
                        Optional<Region> regionOpt = regionRepository.findByNomIgnoreCase(direction.trim());
                        if (regionOpt.isPresent()) {
                            user.setRegion(regionOpt.get());
                        } else {
                            // Créer la région si elle n'existe pas encore
                            Region newRegion = new Region();
                            newRegion.setNom(direction.trim());
                            Region savedRegion = regionRepository.save(newRegion);
                            user.setRegion(savedRegion);
                            log.info("Nouvelle région créée: {}", direction);
                        }
                    }

                    userRepository.save(user);
                    created++;
                    log.info("Compte créé — matricule: {}, rôle: {}", matricule, role);

                    try {
                        emailService.sendWelcomeRHEmail(email.trim().toLowerCase(), nom, prenom, matricule);
                        log.info("📧 Email envoyé à {}", email);
                    } catch (Exception e) {
                        log.error("❌ Échec email pour {} : {}", email, e.getMessage());
                        // Ne pas bloquer l'import si l'email échoue
                    }

                } catch (Exception e) {
                    errors.add("Ligne " + (row.getRowNum() + 1) + " — erreur: " + e.getMessage());
                    skipped++;
                    log.error("Erreur ligne {}: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire le fichier Excel: " + e.getMessage(), e);
        }

        return new ImportResult(created, skipped, errors);
    }

    // ---- Mapping rôles Excel → RoleType ----
    private RoleType mapRole(String roleExcel) {
        if (roleExcel == null) return null;
        return switch (roleExcel.trim()) {
            case "SuperAdmin"        -> RoleType.SUPERADMIN;
            case "Administrateur RH" -> RoleType.ADMIN;
            case "Responsable RH"    -> RoleType.RH_REGIONAL;
            default -> null;
        };
    }

    // ---- Décompose "ABIDI Bassem" en [nom="ABIDI", prenom="Bassem"] ----
    private String[] splitNomPrenom(String nomPrenom) {
        if (nomPrenom == null || nomPrenom.isBlank()) return new String[]{"", ""};
        String[] parts = nomPrenom.trim().split("\\s+", 2);
        if (parts.length == 1) return new String[]{parts[0], ""};
        return new String[]{parts[0], parts[1]};
    }

    // ---- Lecture cellule texte ----
    private String readString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> null;
        };
    }

    // ---- Lecture cellule entier ----
    private Integer readInt(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING  -> {
                try { yield Integer.parseInt(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default -> null;
        };
    }

    // ---- Vérifier si une ligne est vide ----
    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 5; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    // ---- DTO résultat de l'import ----
    public record ImportResult(int created, int skipped, List<String> errors) {}
}