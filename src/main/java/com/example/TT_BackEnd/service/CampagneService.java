package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.CampagneRequestDTO;
import com.example.TT_BackEnd.entity.Campagne;
import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.StatutCampagne;
import com.example.TT_BackEnd.repository.CampagneRepository;
import com.example.TT_BackEnd.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampagneService {

    private final CampagneRepository campagneRepository;
    private final RegionRepository regionRepository;

    // ====================
    // CREATE
    // ====================
    public Campagne creerCampagne(CampagneRequestDTO dto) {
        List<Region> regions = regionRepository.findAllById(dto.getRegionIds());
        if (regions.isEmpty()) {
            throw new RuntimeException("Aucune région trouvée avec les IDs fournis");
        }

        Campagne campagne = new Campagne();
        campagne.setLibelle(dto.getLibelle());
        campagne.setDateDebut(dto.getDateDebut());
        campagne.setDateFin(dto.getDateFin());
        campagne.setBudget(dto.getBudget());
        campagne.setDescription(dto.getDescription());
        campagne.setCode(dto.getCode());
        campagne.setRegions(regions);
        campagne.setStatut(StatutCampagne.BROUILLON);

        return campagneRepository.save(campagne);
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
}