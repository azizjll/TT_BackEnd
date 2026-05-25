package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.CampagneRequestDTO;
import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.CampagneRepository;
import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.repository.StructureRepository;
import com.example.TT_BackEnd.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampagneService {

    private final CampagneRepository campagneRepository;
    private final RegionRepository regionRepository;
    private final ExcelCampagneParser excelCampagneParser;
    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final AuditLogService auditLogService;

    // ====================
    // CREATE
    // ====================
    public Campagne creerCampagne(CampagneRequestDTO dto, String emailCreateur, String ip) {
        Utilisateur createur = utilisateurRepository.findByEmail(emailCreateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Region> regions = regionRepository.findAllById(dto.getRegionIds());
        if (regions.isEmpty()) throw new RuntimeException("Aucune région trouvée");

        Campagne campagne = new Campagne();
        campagne.setLibelle(dto.getLibelle());
        campagne.setDateDebut(dto.getDateDebut());
        campagne.setDateFin(dto.getDateFin());
        campagne.setBudget(dto.getBudget());
        campagne.setDescription(dto.getDescription());
        campagne.setCode(dto.getCode());
        campagne.setRegions(regions);
        campagne.setStatut(StatutCampagne.BROUILLON);
        campagne.setCreateur(createur);

        Campagne saved = campagneRepository.save(campagne);

        auditLogService.log(emailCreateur, "CREATE", "Campagne",
                saved.getId(), null, saved, ip, "SUCCESS");

        return saved;
    }

    // ====================
    // CREATE AVEC EXCEL
    // ====================
    public Campagne creerCampagneAvecExcel(CampagneRequestDTO dto, MultipartFile fichierExcel, String emailCreateur, String ip) {
        Utilisateur createur = utilisateurRepository.findByEmail(emailCreateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + emailCreateur));

        List<Region> regions = excelCampagneParser.extraireRegions(fichierExcel);
        if (regions.isEmpty()) throw new RuntimeException("Aucune région valide");

        Campagne campagne = new Campagne();
        campagne.setLibelle(dto.getLibelle());
        campagne.setDateDebut(dto.getDateDebut());
        campagne.setDateFin(dto.getDateFin());
        campagne.setBudget(dto.getBudget());
        campagne.setDescription(dto.getDescription());
        campagne.setCode(dto.getCode());
        campagne.setRegions(regions);
        campagne.setStatut(StatutCampagne.BROUILLON);
        campagne.setCreateur(createur);

        Campagne campagneSauvee = campagneRepository.save(campagne);

        List<Structure> structures = excelCampagneParser.extraireStructures(fichierExcel);
        structures.forEach(s -> s.setCampagne(campagneSauvee));
        structureRepository.saveAll(structures);

        System.out.println("=== " + structures.size() + " structures sauvées pour campagne ID: " + campagneSauvee.getId());

        auditLogService.log(emailCreateur, "CREATE_EXCEL", "Campagne",
                campagneSauvee.getId(), null, campagneSauvee, ip, "SUCCESS");

        return campagneSauvee;
    }

    // ====================
    // READ
    // ====================
    public List<Campagne> getToutesCampagnes() {
        return campagneRepository.findAll();
    }

    public Campagne getCampagneParId(Long id) {
        return campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));
    }

    public List<Campagne> getCampagnesActives() {
        return campagneRepository.findByStatut(StatutCampagne.ACTIVE);
    }

    // ====================
    // UPDATE
    // ====================
    public Campagne mettreAJourCampagne(Long id, CampagneRequestDTO dto, String email, String ip) {
        Campagne avant = campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        String snapshotAvant = avant.getLibelle() + " | " + avant.getStatut();

        if (dto.getLibelle() != null) avant.setLibelle(dto.getLibelle());
        if (dto.getDateDebut() != null) avant.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) avant.setDateFin(dto.getDateFin());
        if (dto.getBudget() != null) avant.setBudget(dto.getBudget());
        if (dto.getDescription() != null) avant.setDescription(dto.getDescription());
        if (dto.getCode() != null) avant.setCode(dto.getCode());

        if (dto.getStatut() != null) {
            try {
                avant.setStatut(StatutCampagne.valueOf(dto.getStatut().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide : " + dto.getStatut());
            }
        }

        if (dto.getRegionIds() != null && !dto.getRegionIds().isEmpty()) {
            List<Region> regions = regionRepository.findAllById(dto.getRegionIds());
            avant.setRegions(regions);
        }

        Campagne apres = campagneRepository.save(avant);

        auditLogService.log(email, "UPDATE", "Campagne",
                id, snapshotAvant, apres, ip, "SUCCESS");

        return apres;
    }

    // ====================
    // DELETE
    // ====================
    public void supprimerCampagne(Long id, String email, String ip) {
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        auditLogService.log(email, "DELETE", "Campagne",
                id, campagne, null, ip, "SUCCESS");

        campagneRepository.delete(campagne);
    }

    // ====================
    // LOGIQUE METIER
    // ====================
    public Campagne activerCampagne(Long id, String email, String ip) {
        Campagne campagne = getCampagneParId(id);
        if (campagne.getStatut() != StatutCampagne.BROUILLON) {
            throw new RuntimeException("Seulement les campagnes en brouillon peuvent être activées");
        }
        campagne.setStatut(StatutCampagne.ACTIVE);
        Campagne saved = campagneRepository.save(campagne);

        auditLogService.log(email, "ACTIVER", "Campagne",
                id, StatutCampagne.BROUILLON, StatutCampagne.ACTIVE, ip, "SUCCESS");

        return saved;
    }

    public Campagne cloturerCampagne(Long id, String email, String ip) {
        Campagne campagne = getCampagneParId(id);
        if (campagne.getStatut() != StatutCampagne.ACTIVE) {
            throw new RuntimeException("Seules les campagnes actives peuvent être clôturées");
        }
        campagne.setStatut(StatutCampagne.CLOTUREE);
        Campagne saved = campagneRepository.save(campagne);

        auditLogService.log(email, "CLOTURER", "Campagne",
                id, StatutCampagne.ACTIVE, StatutCampagne.CLOTUREE, ip, "SUCCESS");

        return saved;
    }

    public List<Campagne> getCampagnesParCreateur(String email) {
        return campagneRepository.findByCreateurEmail(email);
    }
}