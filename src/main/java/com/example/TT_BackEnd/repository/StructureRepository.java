package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.StatutCampagne;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.entity.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<Structure> findAll();
    Optional<Structure> findByNomAndRegion(String nom, Region region);
    List<Structure> findByRegionIn(List<Region> regions);
    @Query("SELECT s FROM Structure s WHERE s.region IN " +
            "(SELECT r FROM Campagne c JOIN c.regions r WHERE c.statut = 'ACTIVE')")
    List<Structure> findStructuresDeCampagneActive();

    @Query("SELECT DISTINCT s FROM Structure s WHERE s.region IN " +
            "(SELECT r FROM Campagne c JOIN c.regions r WHERE c.id = :campagneId)")
    List<Structure> findStructuresByCampagneId(@Param("campagneId") Long campagneId);

    // ← NOUVEAU : filtrer directement par campagne
    List<Structure> findByCampagneId(Long campagneId);

    // ← NOUVEAU : filtrer par campagne et statut
    List<Structure> findByCampagneStatut(StatutCampagne statut);

    List<Structure> findByRegionIdAndCampagneId(Long regionId, Long campagneId);
    List<Structure> findByCampagneIdAndRegionId(Long campagneId, Long regionId);

}