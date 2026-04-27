package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.service.ParentAutoriseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@CrossOrigin("*")
public class ParentAutoriseController {

    private final ParentAutoriseService parentService;

    public ParentAutoriseController(ParentAutoriseService parentService) {
        this.parentService = parentService;
    }

    // 📋 GET ALL
    @GetMapping
    public ResponseEntity<?> getAllParents() {
        return ResponseEntity.ok(parentService.getAllParents());
    }

    // 🔍 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getParent(@PathVariable Long id) {
        return ResponseEntity.ok(parentService.getParentById(id));
    }

    // ➕ ADD
    @PostMapping
    public ResponseEntity<?> addParent(
            @RequestParam String nomPrenom,
            @RequestParam String matricule,
            @RequestParam int autorises          // 🆕
    ) {
        try {
            return ResponseEntity.ok(
                    parentService.addParent(nomPrenom.trim(), matricule.trim(), autorises)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParent(
            @PathVariable Long id,
            @RequestParam String nomPrenom,
            @RequestParam String matricule,
            @RequestParam int autorises,         // 🆕 int au lieu de boolean
            @RequestParam int utilise            // 🆕 int au lieu de boolean
    ) {
        try {
            return ResponseEntity.ok(
                    parentService.updateParent(id, nomPrenom.trim(), matricule.trim(), autorises, utilise)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ❌ DELETE (optionnel)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
        return ResponseEntity.ok("Supprimé ✅");
    }
}