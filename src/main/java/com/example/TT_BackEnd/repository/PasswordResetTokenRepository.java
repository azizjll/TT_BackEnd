package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.PasswordResetToken;
import com.example.TT_BackEnd.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(Utilisateur user);
    void deleteByUser(Utilisateur user);
}
