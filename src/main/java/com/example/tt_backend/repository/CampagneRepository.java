package com.example.tt_backend.repository;

import com.example.tt_backend.entity.Campagne;
import com.example.tt_backend.entity.StatutCampagne;
import com.example.tt_backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampagneRepository extends JpaRepository<Campagne, Long> {

    // Recherche par statut
    List<Campagne> findByStatut(StatutCampagne statut);


    List<Campagne> findByCreateurEmail(String email);


    List<Campagne> findByCreateur(Utilisateur createur);
    Optional<Campagne> findByCodeAndStatut(String code, StatutCampagne statut);


}