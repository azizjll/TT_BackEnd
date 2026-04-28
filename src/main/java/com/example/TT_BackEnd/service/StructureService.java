package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // ── Campagne ACTIVE (peu importe le créateur) ─────────────
        List<Campagne> campagnes = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE); // ← plus de createurId

        if (campagnes.isEmpty()) {
            System.out.println("=== Aucune campagne active");
            return List.of();
        }

        Long campagneActiveId = campagnes.get(0).getId();

        // ── Filtrer par région du RH_REGIONAL ────────────────────
        if (utilisateur.getRole() == RoleType.RH_REGIONAL
                && utilisateur.getRegion() != null) {

            Long regionId = utilisateur.getRegion().getId();
            System.out.println("=== RH_REGIONAL région ID : " + regionId);
            return structureRepository
                    .findByCampagneIdAndRegionId(campagneActiveId, regionId); // ← nouveau
        }

        // ── Admin / autres rôles : toutes les structures ──────────
        return structureRepository.findByCampagneId(campagneActiveId);
    }

    // ← version sans SecurityContextHolder (publique)
    public List<Structure> getStructuresCampagneActivePublique() {
        List<Campagne> campagnesActives = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE);

        if (campagnesActives.isEmpty()) {
            return List.of();
        }

        Long campagneActiveId = campagnesActives.get(0).getId();
        return structureRepository.findByCampagneId(campagneActiveId);
    }


}