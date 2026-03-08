package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.Saisonnier;
import com.example.TT_BackEnd.entity.StatutCandidature;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaisonnierRepository extends JpaRepository<Saisonnier, Long> {

    List<Saisonnier> findByRegionId(Long regionId);

    List<Saisonnier> findByRegion(Region region);

    @Query("SELECT s FROM Saisonnier s JOIN s.candidatures c WHERE c.statut = :statut AND s.region = :region")
    List<Saisonnier> findSaisonniersAcceptesParRegion(
            @Param("region") Region region,
            @Param("statut") StatutCandidature statut
    );

}