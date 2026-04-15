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
    private final StructureRepository structureRepository; // ← AJOUTER




    // ====================
    // CREATE
    // ====================
    public Campagne creerCampagne(CampagneRequestDTO dto, String emailCreateur) {
        // Récupérer l'utilisateur connecté
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
        campagne.setCreateur(createur);   // ← association

        return campagneRepository.save(campagne);
    }

    /**
     * Crée une campagne et l'affecte automatiquement aux régions extraites du fichier Excel.
     */
    public Campagne creerCampagneAvecExcel(CampagneRequestDTO dto, MultipartFile fichierExcel, String emailCreateur) {

        Utilisateur createur = utilisateurRepository.findByEmail(emailCreateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + emailCreateur));

        // 1. Extraire les régions
        List<Region> regions = excelCampagneParser.extraireRegions(fichierExcel);
        if (regions.isEmpty()) throw new RuntimeException("Aucune région valide");

        // 2. Créer et sauvegarder la campagne
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

        Campagne campagneSauvee = campagneRepository.save(campagne); // ← sauvegarder d'abord

        // 3. Extraire les structures et les lier à la campagne
        List<Structure> structures = excelCampagneParser.extraireStructures(fichierExcel);
        structures.forEach(s -> s.setCampagne(campagneSauvee)); // ← associer la campagne
        structureRepository.saveAll(structures); // ← sauvegarder avec campagne_id

        System.out.println("=== " + structures.size() + " structures sauvées pour campagne ID: " + campagneSauvee.getId());

        return campagneSauvee;
    }    // ====================
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
    public Campagne mettreAJourCampagne(Long id, CampagneRequestDTO dto) {
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        if (dto.getLibelle() != null) campagne.setLibelle(dto.getLibelle());
        if (dto.getDateDebut() != null) campagne.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) campagne.setDateFin(dto.getDateFin());
        if (dto.getBudget() != null) campagne.setBudget(dto.getBudget());
        if (dto.getDescription() != null) campagne.setDescription(dto.getDescription());
        if (dto.getCode() != null) campagne.setCode(dto.getCode());

        if (dto.getRegionIds() != null && !dto.getRegionIds().isEmpty()) {
            List<Region> regions = regionRepository.findAllById(dto.getRegionIds());
            campagne.setRegions(regions);
        }

        return campagneRepository.save(campagne);
    }

    // ====================
    // DELETE
    // ====================
    public void supprimerCampagne(Long id) {
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));
        campagneRepository.delete(campagne);
    }

    // ====================
    // LOGIQUE METIER
    // ====================
    public Campagne activerCampagne(Long id) {
        Campagne campagne = getCampagneParId(id);
        if (campagne.getStatut() != StatutCampagne.BROUILLON) {
            throw new RuntimeException("Seulement les campagnes en brouillon peuvent être activées");
        }
        campagne.setStatut(StatutCampagne.ACTIVE);
        return campagneRepository.save(campagne);
    }

    public Campagne cloturerCampagne(Long id) {
        Campagne campagne = getCampagneParId(id);
        if (campagne.getStatut() != StatutCampagne.ACTIVE) {
            throw new RuntimeException("Seules les campagnes actives peuvent être clôturées");
        }
        campagne.setStatut(StatutCampagne.CLOTUREE);
        return campagneRepository.save(campagne);
    }

    public List<Campagne> getCampagnesParCreateur(String email) {
        return campagneRepository.findByCreateurEmail(email);
    }
}