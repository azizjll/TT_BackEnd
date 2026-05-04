package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.AnalyseIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalyseIARepository extends JpaRepository<AnalyseIA, Long> {
    Optional<AnalyseIA> findByCandidatureId(Long candidatureId);
}
