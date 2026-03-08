package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByNom(String nom);
}
