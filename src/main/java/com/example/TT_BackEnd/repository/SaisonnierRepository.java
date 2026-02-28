package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Saisonnier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaisonnierRepository extends JpaRepository<Saisonnier, Long> {
    List<Saisonnier> findByRegionId(Long regionId);
}