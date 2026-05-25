package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.AuditLog;
import com.example.TT_BackEnd.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async   // ← écriture asynchrone, pas d'impact sur les performances
    public void log(String email, String action, String entite,
                    Long entiteId, Object avant, Object apres,
                    String ip, String statut) {
        try {
            AuditLog log = new AuditLog();
            log.setUtilisateurEmail(email);
            log.setAction(action);
            log.setEntite(entite);
            log.setEntiteId(entiteId);
            log.setAdresseIp(ip);
            log.setStatut(statut);

            if (avant != null)
                log.setDonneesAvant(objectMapper.writeValueAsString(avant));
            if (apres != null)
                log.setDonneesApres(objectMapper.writeValueAsString(apres));

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Ne jamais bloquer le flux métier pour un log raté
            log.error("Erreur lors de l'enregistrement du log d'audit", e);
        }
    }

    public List<AuditLog> getTousLesLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsParUtilisateur(String email) {
        return auditLogRepository.findByUtilisateurEmailOrderByTimestampDesc(email);
    }

    public List<AuditLog> getLogsParEntite(String entite, Long id) {
        return auditLogRepository.findByEntiteAndEntiteIdOrderByTimestampDesc(entite, id);
    }
}