package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.ParentAutorise;
import com.example.TT_BackEnd.repository.ParentAutoriseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParentAutoriseService {

    private final ParentAutoriseRepository parentRepo;

    public ParentAutoriseService(ParentAutoriseRepository parentRepo) {
        this.parentRepo = parentRepo;
    }

    // 📋 1. Get all parents
    public List<ParentAutorise> getAllParents() {
        return parentRepo.findAll();
    }

    // 🔍 2. Get parent by ID
    public ParentAutorise getParentById(Long id) {
        return parentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé"));
    }

    // ➕ 3. Ajouter parent
    public ParentAutorise addParent(String nomPrenom, String matricule) {

        if (parentRepo.existsByMatricule(matricule)) {
            throw new RuntimeException("Matricule déjà existant ❌");
        }

        ParentAutorise parent = new ParentAutorise();
        parent.setNomPrenom(nomPrenom);
        parent.setMatricule(matricule);
        parent.setUtilise(false);

        return parentRepo.save(parent);
    }

    // ✏️ 4. Modifier parent
    public ParentAutorise updateParent(Long id, String nomPrenom, String matricule, boolean utilise) {

        ParentAutorise parent = parentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé"));

        if (!parent.getMatricule().equals(matricule)
                && parentRepo.existsByMatricule(matricule)) {
            throw new RuntimeException("Matricule déjà utilisé ❌");
        }

        parent.setNomPrenom(nomPrenom);
        parent.setMatricule(matricule);

        // 🔥 AJOUT IMPORTANT
        parent.setUtilise(utilise);

        return parentRepo.save(parent);
    }


    // ❌ 5. Supprimer (optionnel)
    public void deleteParent(Long id) {
        parentRepo.deleteById(id);
    }
}
