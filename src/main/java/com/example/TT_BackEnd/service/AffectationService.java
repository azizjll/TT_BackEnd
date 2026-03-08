package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AffectationService {

    @Autowired
    private AffectationRepository affectationRepository;

    @Autowired
    private SaisonnierRepository saisonnierRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private CampagneRepository campagneRepository;

    public void affecterSaisonnier(Long saisonnierId, Long structureId, Long campagneId) {

        Saisonnier saisonnier = saisonnierRepository.findById(saisonnierId)
                .orElseThrow(() -> new RuntimeException("Saisonnier introuvable"));

        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure introuvable"));

        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        // vérifier même région
        if (!saisonnier.getRegion().getId().equals(structure.getRegion().getId())) {
            throw new RuntimeException("Région invalide");
        }

        Affectation aff = new Affectation();

        aff.setSaisonnier(saisonnier);
        aff.setStructure(structure);
        aff.setCampagne(campagne);
        aff.setDateAffectation(LocalDate.now());

        affectationRepository.save(aff);
    }
}