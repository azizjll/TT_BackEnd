package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.SaisonnierDTO;
import com.example.TT_BackEnd.dto.UpdatePaieRequest;
import com.example.TT_BackEnd.entity.Saisonnier;
import com.example.TT_BackEnd.repository.AffectationRepository;
import com.example.TT_BackEnd.repository.SaisonnierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaisonnierService {

    private final SaisonnierRepository repo;
    private final AffectationRepository affectationRepo;
    private final AuditLogService auditLogService;  // 🆕

    // ====================
    // READ
    // ====================
    public List<SaisonnierDTO> findAll(String email, String ip) {
        List<SaisonnierDTO> result = repo.findAll()
                .stream()
                .map(s -> SaisonnierDTO.from(s, null))
                .collect(Collectors.toList());

        // 🆕 AUDIT
        auditLogService.log(email, "READ_ALL", "Saisonnier",
                null, null, result.size() + " résultats", ip, "SUCCESS");

        return result;
    }

    public SaisonnierDTO findById(Long id, String email, String ip) {
        SaisonnierDTO result = repo.findById(id)
                .map(s -> SaisonnierDTO.from(s, null))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Saisonnier " + id + " introuvable"));

        // 🆕 AUDIT
        auditLogService.log(email, "READ", "Saisonnier",
                id, null, result, ip, "SUCCESS");

        return result;
    }

    public List<SaisonnierDTO> findByCampagneAndRegion(Long campagneId, Long regionId, String email, String ip) {
        List<SaisonnierDTO> result = repo.findAll()
                .stream()
                .filter(s -> s.getRegion() != null && s.getRegion().getId().equals(regionId))
                .flatMap(s -> s.getCandidatures().stream()
                        .filter(c -> c.getCampagne().getId().equals(campagneId)
                                && c.getStatut().name().equals("ACCEPTEE"))
                        .map(c -> SaisonnierDTO.from(s, c.getStatut().name())))
                .collect(Collectors.toList());

        // 🆕 AUDIT
        auditLogService.log(email, "READ_BY_CAMPAGNE_REGION", "Saisonnier",
                campagneId, null,
                result.size() + " résultats — regionId: " + regionId,
                ip, "SUCCESS");

        return result;
    }

    public List<SaisonnierDTO> findByCampagneAndStructure(Long campagneId, Long structureId, String email, String ip) {
        List<Long> saisonnierIds = affectationRepo.findAll()
                .stream()
                .filter(a -> a.getCampagne().getId().equals(campagneId)
                        && a.getStructure().getId().equals(structureId))
                .map(a -> a.getSaisonnier().getId())
                .collect(Collectors.toList());

        List<SaisonnierDTO> result = repo.findAllById(saisonnierIds)
                .stream()
                .flatMap(s -> s.getCandidatures().stream()
                        .filter(c -> c.getCampagne().getId().equals(campagneId)
                                && c.getStatut().name().equals("ACCEPTEE"))
                        .map(c -> SaisonnierDTO.from(s, c.getStatut().name())))
                .collect(Collectors.toList());

        // 🆕 AUDIT
        auditLogService.log(email, "READ_BY_CAMPAGNE_STRUCTURE", "Saisonnier",
                campagneId, null,
                result.size() + " résultats — structureId: " + structureId,
                ip, "SUCCESS");

        return result;
    }

    // ====================
    // UPDATE
    // ====================
    public SaisonnierDTO updateAbsences(Long id, UpdatePaieRequest req, String email, String ip) {
        Saisonnier s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Saisonnier " + id + " introuvable"));

        // snapshot avant
        String snapshotAvant = "absences: " + s.getAbsences();

        if (req.getAbsences() != null) s.setAbsences(req.getAbsences());
        repo.save(s);

        SaisonnierDTO result = SaisonnierDTO.from(s, null);

        // 🆕 AUDIT
        auditLogService.log(email, "UPDATE_ABSENCES", "Saisonnier",
                id, snapshotAvant, "absences: " + s.getAbsences(), ip, "SUCCESS");

        return result;
    }
}