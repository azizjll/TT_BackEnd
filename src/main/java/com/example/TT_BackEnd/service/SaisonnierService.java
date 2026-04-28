package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.SaisonnierDTO;
import com.example.TT_BackEnd.repository.AffectationRepository;
import com.example.TT_BackEnd.repository.SaisonnierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

// SaisonnierService.java
@Service
@RequiredArgsConstructor

public class SaisonnierService {

    private final SaisonnierRepository repo;
    private final AffectationRepository affectationRepo;  // ← ajouter



    public List<SaisonnierDTO> findAll() {
        return repo.findAll()
                .stream()
                .map(SaisonnierDTO::from)
                .collect(Collectors.toList());
    }

    public SaisonnierDTO findById(Long id) {
        return repo.findById(id)
                .map(SaisonnierDTO::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Saisonnier " + id + " introuvable"));
    }

    public List<SaisonnierDTO> findByCampagneAndRegion(Long campagneId, Long regionId) {
        return repo.findAll()
                .stream()
                .filter(s -> s.getRegion() != null && s.getRegion().getId().equals(regionId))
                .filter(s -> s.getCandidatures() != null && s.getCandidatures()
                        .stream()
                        .anyMatch(c -> c.getCampagne().getId().equals(campagneId)))
                .map(SaisonnierDTO::from)
                .collect(Collectors.toList());
    }

    public List<SaisonnierDTO> findByCampagneAndStructure(Long campagneId, Long structureId) {
        // Les saisonniers affectés à cette structure pour cette campagne
        List<Long> saisonnierIds = affectationRepo.findAll()
                .stream()
                .filter(a -> a.getCampagne().getId().equals(campagneId)
                        && a.getStructure().getId().equals(structureId))
                .map(a -> a.getSaisonnier().getId())
                .collect(Collectors.toList());

        return repo.findAllById(saisonnierIds)
                .stream()
                .map(SaisonnierDTO::from)
                .collect(Collectors.toList());
    }



}