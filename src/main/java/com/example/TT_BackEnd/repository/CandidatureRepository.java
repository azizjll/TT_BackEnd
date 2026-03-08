package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findBySaisonnierRegionId(Long regionId);
    List<Candidature> findByCampagneIdAndSaisonnierRegionId(Long campagneId, Long regionId);
}
