package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUtilisateurEmailOrderByTimestampDesc(String email);

    List<AuditLog> findByEntiteOrderByTimestampDesc(String entite);

    List<AuditLog> findByEntiteAndEntiteIdOrderByTimestampDesc(String entite, Long entiteId);

    List<AuditLog> findAllByOrderByTimestampDesc();
}