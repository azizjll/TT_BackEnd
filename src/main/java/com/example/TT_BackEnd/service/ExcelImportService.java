package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.CampagneRepository;
import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.repository.StructureRepository;
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
    private final CampagneRepository campagneRepository;
    private final StructureRepository structureRepository;


    public ImportResult importFromExcel(MultipartFile file) {
        int created = 0;
        int skipped = 0;
        int deleted = 0;  // ← nouveau compteur
        List<String> errors = new ArrayList<>();

        // ── Collecter tous les matricules présents dans le fichier Excel ──
        Set<Integer> matriculesInFile = new HashSet<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (rows.hasNext()) rows.next(); // sauter l'en-tête

            // ── PASSE 1 : collecter les matricules du fichier ──────────────
            for (Row row : sheet) {
                if (isRowEmpty(row)) continue;
                Integer matricule = readInt(row.getCell(0));
                if (matricule != null) {
                    matriculesInFile.add(matricule);
                }
            }

            // ── SUPPRESSION : utilisateurs en base absents du fichier ──────
            // On exclut le SUPERADMIN de la suppression (sécurité)
            List<Utilisateur> toDelete = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != RoleType.SUPERADMIN)
                    .filter(u -> !matriculesInFile.contains(u.getMatricule()))
                    .toList();

            for (Utilisateur u : toDelete) {
                userRepository.delete(u);
                deleted++;
                log.info("Compte supprimé — matricule: {}, email: {}", u.getMatricule(), u.getEmail());
            }

            // ── PASSE 2 : créer / ignorer les utilisateurs du fichier ──────
            rows = sheet.iterator();
            if (rows.hasNext()) rows.next(); // re-sauter l'en-tête

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row)) continue;

                try {
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

                    boolean matriculeExiste = !userRepository.findAllByMatricule(matricule).isEmpty();
                    boolean emailExiste     = !userRepository.findAllByEmail(email.trim().toLowerCase()).isEmpty();

                    if (matriculeExiste || emailExiste) {
                        log.info("Utilisateur déjà existant, ignoré — matricule: {}", matricule);
                        skipped++;
                        continue;
                    }

                    RoleType role = mapRole(roleExcel);
                    if (role == null) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " — rôle inconnu: " + roleExcel);
                        skipped++;
                        continue;
                    }

                    String[] parts = splitNomPrenom(nomPrenom);
                    String nom    = parts[0];
                    String prenom = parts[1];

                    Utilisateur user = new Utilisateur();
                    user.setNom(nom);
                    user.setPrenom(prenom);
                    user.setEmail(email.trim().toLowerCase());
                    user.setMatricule(matricule);
                    user.setPassword(passwordEncoder.encode(String.valueOf(matricule)));
                    user.setRole(role);
                    user.setEnabled(true);

                    if (role == RoleType.RH_REGIONAL && direction != null && !direction.isBlank()) {
                        Optional<Region> regionOpt = regionRepository.findByNomIgnoreCase(direction.trim());
                        Region region = regionOpt.orElseGet(() -> {
                            Region r = new Region();
                            r.setNom(direction.trim());
                            log.info("Nouvelle région créée: {}", direction);
                            return regionRepository.save(r);
                        });
                        user.setRegion(region);
                    }

                    userRepository.save(user);
                    created++;
                    log.info("Compte créé — matricule: {}, rôle: {}", matricule, role);

                    try {
                        emailService.sendWelcomeRHEmail(email.trim().toLowerCase(), nom, prenom, matricule);
                        log.info("📧 Email envoyé à {}", email);
                    } catch (Exception e) {
                        log.error("❌ Échec email pour {} : {}", email, e.getMessage());
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

        return new ImportResult(created, skipped, deleted, errors);  // ← deleted ajouté
    }

    // ── DTO mis à jour ──────────────────────────────────────────────────
    public record ImportResult(int created, int skipped, int deleted, List<String> errors) {}

    // ---- Mapping rôles Excel → RoleType ----
    private RoleType mapRole(String roleExcel) {
        if (roleExcel == null) return null;
        return switch (roleExcel.trim()) {
            case "SuperAdmin"             -> RoleType.SUPERADMIN;
            case "Administrateur RH"      -> RoleType.ADMIN;
            case "Responsable RH"         -> RoleType.RH_REGIONAL;
            case "Responsable Structure"  -> RoleType.RESPONSABLE_STRUCTURE;
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




    public ImportResult importResponsablesStructure(MultipartFile file) {
        int created = 0;
        int skipped = 0;
        int deleted = 0;
        List<String> errors = new ArrayList<>();

        // ── Vérifier qu'il existe une campagne ACTIVE ──────────────────────
        List<Campagne> campagnesActives = campagneRepository.findByStatut(StatutCampagne.ACTIVE);
        if (campagnesActives.isEmpty()) {
            throw new RuntimeException("Aucune campagne active. L'import est impossible.");
        }
        Campagne campagneActive = campagnesActives.get(0); // prendre la première active

        Set<Integer> matriculesInFile = new HashSet<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // ── PASSE 1 : collecter les matricules du fichier ──────────────
            for (Row row : sheet) {
                if (isRowEmpty(row)) continue;
                Integer matricule = readInt(row.getCell(0));
                if (matricule != null) matriculesInFile.add(matricule);
            }

            // ── SUPPRESSION : RS en base absents du fichier ────────────────
            // On ne supprime que les RESPONSABLE_STRUCTURE (pas les autres rôles)
            List<Utilisateur> toDelete = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == RoleType.RESPONSABLE_STRUCTURE)
                    .filter(u -> u.getCampagne() != null
                            && campagneActive.getId().equals(u.getCampagne().getId()))
                    .filter(u -> !matriculesInFile.contains(u.getMatricule()))
                    .toList();

            for (Utilisateur u : toDelete) {
                userRepository.delete(u);
                deleted++;
                log.info("Compte RS supprimé — matricule: {}, email: {}", u.getMatricule(), u.getEmail());
            }

            // ── PASSE 2 : créer / ignorer ──────────────────────────────────
            Iterator<Row> rows = sheet.iterator();
            if (rows.hasNext()) rows.next(); // sauter l'en-tête

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row)) continue;

                try {
                    Integer matricule     = readInt(row.getCell(0));
                    String  nomPrenom     = readString(row.getCell(1));
                    String  email         = readString(row.getCell(2));
                    String  roleExcel     = readString(row.getCell(3));
                    String  nomStructure  = readString(row.getCell(4)); // colonne "Structures d'affectation"

                    // ── Validations de base ────────────────────────────────
                    if (matricule == null || email == null || email.isBlank()) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " ignorée: matricule ou email manquant");
                        skipped++;
                        continue;
                    }

                    RoleType role = mapRole(roleExcel);
                    if (role != RoleType.RESPONSABLE_STRUCTURE) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " ignorée: rôle '" + roleExcel + "' non autorisé dans cet import");
                        skipped++;
                        continue;
                    }

                    if (nomStructure == null || nomStructure.isBlank()) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " ignorée: structure manquante");
                        skipped++;
                        continue;
                    }

                    // ── Trouver la structure dans les régions de la campagne active ─
                    // La campagne active a une liste de régions → chercher la structure dans ces régions
                    Structure structure = null;
                    Region regionTrouvee = null;

                    for (Region region : campagneActive.getRegions()) {
                        Optional<Structure> structOpt = structureRepository
                                .findByNomIgnoreCaseAndRegionAndCampagneId(
                                        nomStructure.trim(),
                                        region,
                                        campagneActive.getId()
                                );
                        if (structOpt.isPresent()) {
                            structure = structOpt.get();
                            regionTrouvee = region;
                            break;
                        }
                    }

                    if (structure == null) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + " ignorée: structure '" + nomStructure
                                + "' introuvable dans les régions de la campagne active");
                        skipped++;
                        continue;
                    }

                    // ── Ignorer si déjà existant ───────────────────────────
                    boolean existeDansCetteCampagne = userRepository
                            .findAllByMatricule(matricule)
                            .stream()
                            .anyMatch(u -> u.getCampagne() != null
                                    && campagneActive.getId().equals(u.getCampagne().getId()));

                    if (existeDansCetteCampagne) {
                        log.info("RS déjà existant pour cette campagne, ignoré — matricule: {}", matricule);
                        skipped++;
                        continue;
                    }

                    // ── Créer l'utilisateur ────────────────────────────────
                    String[] parts = splitNomPrenom(nomPrenom);
                    String nom    = parts[0];
                    String prenom = parts[1];

                    Utilisateur user = new Utilisateur();
                    user.setNom(nom);
                    user.setPrenom(prenom);
                    user.setEmail(email.trim().toLowerCase());
                    user.setMatricule(matricule);
                    user.setPassword(passwordEncoder.encode(String.valueOf(matricule)));
                    user.setRole(RoleType.RESPONSABLE_STRUCTURE);
                    user.setEnabled(true);
                    user.setRegion(regionTrouvee);      // région déduite de la structure
                    user.setStructure(structure);        // affectation directe à la structure
                    user.setCampagne(campagneActive);

                    userRepository.save(user);
                    created++;
                    log.info("Compte RS créé — matricule: {}, structure: {}, région: {}",
                            matricule, structure.getNom(), regionTrouvee.getNom());

                    // ❌ Pas d'envoi d'email pour les Responsables Structure

                } catch (Exception e) {
                    errors.add("Ligne " + (row.getRowNum() + 1) + " — erreur: " + e.getMessage());
                    skipped++;
                    log.error("Erreur ligne {}: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

        } catch (RuntimeException e) {
            throw e; // relancer l'exception "pas de campagne active"
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire le fichier Excel: " + e.getMessage(), e);
        }

        return new ImportResult(created, skipped, deleted, errors);
    }
}