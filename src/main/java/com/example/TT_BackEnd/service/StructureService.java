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

        // ── 1. Email de l'utilisateur connecté via JWT ────────────────
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        System.out.println("=== Utilisateur connecté : " + email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        System.out.println("=== Utilisateur ID : " + utilisateur.getId());

        // ── 2. Campagne ACTIVE de CET utilisateur seulement ──────────
        List<Campagne> campagnes = campagneRepository
                .findByStatutAndCreateurId(StatutCampagne.ACTIVE, utilisateur.getId());

        System.out.println("=== Campagnes actives trouvées : " + campagnes.size());

        if (campagnes.isEmpty()) {
            System.out.println("=== Aucune campagne active pour : " + email);
            return List.of(); // liste vide — pas d'erreur 500
        }

        Long campagneActiveId = campagnes.get(0).getId();
        System.out.println("=== Campagne active ID : " + campagneActiveId);

        // ── 3. Structures liées à cette campagne ─────────────────────
        List<Structure> structures = structureRepository.findByCampagneId(campagneActiveId);
        System.out.println("=== Structures trouvées : " + structures.size());

        return structures;
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