package com.example.tt_backend.repository;

import com.example.tt_backend.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    long countByStructureIdAndCampagneId(Long structureId, Long campagneId);

    List<Affectation> findByCampagneIdAndStructureId(Long campagneId, Long structureId);

    Optional<Affectation> findTopBySaisonnierIdOrderByDateAffectationDesc(Long saisonnierId);



}