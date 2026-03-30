package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.SaisonnierDTO;
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
}