package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.RoleType;
import com.example.TT_BackEnd.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    List<Utilisateur> findByRegionId(Long regionId);

    // Ajoute cette méthode pour signin / forgot password
    Optional<Utilisateur> findByEmail(String email);


    List<Utilisateur> findByRegionAndRole(Region region, RoleType roleType);
}
