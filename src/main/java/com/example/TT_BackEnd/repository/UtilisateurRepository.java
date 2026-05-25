package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.RoleType;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    List<Utilisateur> findByRegionId(Long regionId);

    // Ajoute cette méthode pour signin / forgot password
    Optional<Utilisateur> findByEmail(String email);


    List<Utilisateur> findByRegionAndRole(Region region, RoleType roleType);

    List<Utilisateur> findByRole(RoleType role);
    Optional<Utilisateur> findByMatricule(Integer matricule);



    // ── Pour l'import — JPQL explicite, retourne toujours une List ─
    @Query("SELECT u FROM Utilisateur u WHERE u.matricule = :matricule")
    List<Utilisateur> findAllByMatricule(@Param("matricule") Integer matricule);

    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)")
    List<Utilisateur> findAllByEmail(@Param("email") String email);

    // ── Pour la campagne ───────────────────────────────────────────
    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.campagne.id = :campagneId")
    List<Utilisateur> findByRoleAndCampagneId(
            @Param("role") com.example.TT_BackEnd.entity.RoleType role,
            @Param("campagneId") Long campagneId);


    // UtilisateurRepository.java
    @Query("SELECT u FROM Utilisateur u WHERE u.matricule = :matricule AND u.campagne.statut = 'ACTIVE'")
    Optional<Utilisateur> findByMatriculeAndCampagneActive(@Param("matricule") Integer matricule);

}
