package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.VerificationToken;
import com.example.TT_BackEnd.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(Utilisateur user);
}
