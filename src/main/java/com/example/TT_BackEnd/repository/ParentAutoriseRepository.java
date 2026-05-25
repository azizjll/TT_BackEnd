package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.ParentAutorise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentAutoriseRepository extends JpaRepository<ParentAutorise, Long> {

    Optional<ParentAutorise> findByNomPrenomAndMatricule(String nomPrenom, String matricule);
    Optional<ParentAutorise> findByMatricule(String matricule); // 🆕
    boolean existsByMatricule(String matricule);
    List<ParentAutorise> findByCampagneId(Long campagneId);  // ← AJOUTER
    boolean existsByMatriculeAndCampagneId(String matricule, Long campagneId); // ← AJOUTER
    // ParentAutoriseRepository.java
    Optional<ParentAutorise> findByMatriculeAndCampagneId(String matricule, Long campagneId);
    Optional<ParentAutorise> findByNomPrenomAndMatriculeAndCampagneId(
            String nomPrenom, String matricule, Long campagneId);
}