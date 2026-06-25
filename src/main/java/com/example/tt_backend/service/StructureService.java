package com.example.tt_backend.service;

import com.example.tt_backend.dto.StructurePubliqueDTO;
import com.example.tt_backend.entity.*;
import com.example.tt_backend.exception.CampagneInvalideException;
import com.example.tt_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StructureService {

    private final StructureRepository structureRepository;
    private final CampagneRepository campagneRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<Structure> getStructuresCampagneActive() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Campagne> campagnes = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE);

        if (campagnes.isEmpty()) {
            log.warn("Aucune campagne active trouvée");
            return List.of();
        }

        Long campagneActiveId = campagnes.get(0).getId();

        if (utilisateur.getRole() == RoleType.RH_REGIONAL
                && utilisateur.getRegion() != null) {

            Long regionId = utilisateur.getRegion().getId();

            log.info("RH_REGIONAL connecté - regionId={}", regionId);

            return structureRepository
                    .findByCampagneIdAndRegionId(campagneActiveId, regionId);
        }

        log.info("Accès admin/autres rôles - toutes structures campagneId={}", campagneActiveId);

        return structureRepository.findByCampagneId(campagneActiveId);
    }

    public List<StructurePubliqueDTO> getStructuresParCodeCampagne(String code) {

        Campagne campagne = campagneRepository
                .findByCodeAndStatut(code, StatutCampagne.ACTIVE)
                .orElseThrow(CampagneInvalideException::new);

        LocalDate today = LocalDate.now();
        if (today.isBefore(campagne.getDateDebut()) || today.isAfter(campagne.getDateFin())) {
            throw new CampagneInvalideException();
        }

        log.info("Accès public structures via code campagne (id={})", campagne.getId());

        return structureRepository.findByCampagneId(campagne.getId())
                .stream()
                .map(s -> new StructurePubliqueDTO(
                        s.getId(), s.getNom(), s.getType().name(),
                        s.getRegion().getNom(), s.getAdresse(),
                        s.isDisponiblePourCandidature()))
                .toList();
    }
}