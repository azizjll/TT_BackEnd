package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    long countByStructureIdAndCampagneId(Long structureId, Long campagneId);

}