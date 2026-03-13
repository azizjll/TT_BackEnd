package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.entity.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {

    @Query("SELECT DISTINCT s.region FROM Structure s")
    List<Region> findAllRegionsWithStructures();

    List<Structure> findByRegionAndType(Region region, StructureType structureType);

    List<Structure> findByRegionId(Long regionId);

    boolean existsByNom(String nom);

    Optional<Structure> findByNom(String nom);
}