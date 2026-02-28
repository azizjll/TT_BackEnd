package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Campagne;
import com.example.TT_BackEnd.entity.StatutCampagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampagneRepository extends JpaRepository<Campagne, Long> {

    // Recherche par statut
    List<Campagne> findByStatut(StatutCampagne statut);

    // Recherche pour une seule région
    List<Campagne> findByRegions_Id(Long regionId);

    // Recherche pour plusieurs régions
    List<Campagne> findByRegions_IdIn(List<Long> regionIds);
}