package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.ParentAutorise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentAutoriseRepository extends JpaRepository<ParentAutorise, Long> {

    Optional<ParentAutorise> findByNomPrenomAndMatricule(String nomPrenom, String matricule);
    Optional<ParentAutorise> findByMatricule(String matricule); // 🆕
    boolean existsByMatricule(String matricule);
}