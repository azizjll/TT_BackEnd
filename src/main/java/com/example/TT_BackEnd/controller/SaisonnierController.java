package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.SaisonnierDTO;
import com.example.TT_BackEnd.service.SaisonnierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// SaisonnierController.java
@RestController
@RequestMapping("/api/saisonniers")
@CrossOrigin("*")   // adapte selon ton env
@RequiredArgsConstructor
public class SaisonnierController {

    private final SaisonnierService service;

    @GetMapping
    public ResponseEntity<List<SaisonnierDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaisonnierDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}