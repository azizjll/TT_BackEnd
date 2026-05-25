package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.EtatRHDTO;
import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.CampagneRepository;
import com.example.TT_BackEnd.repository.EtatRHRepository;
import com.example.TT_BackEnd.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtatRHService {

    private final EtatRHRepository etatRHRepository;
    private final CampagneRepository campagneRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CloudinaryService cloudinaryService;
    private final AuditLogService auditLogService;  // 🆕

    // ── RH_REGIONAL : uploader son état ──────────────────────────
    public EtatRH uploadEtat(MultipartFile file, String email, String ip) {

        System.out.println("=== Email JWT : " + email);

        Utilisateur rh = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        System.out.println("=== RH trouvé : " + rh.getId() + " | région : " + rh.getRegion());

        if (rh.getRegion() == null) {
            throw new RuntimeException("Aucune région associée à ce compte RH");
        }

        // ── Campagne active ───────────────────────────────────────
        List<Campagne> campagnes = campagneRepository.findByStatut(StatutCampagne.ACTIVE);
        System.out.println("=== Campagnes actives : " + campagnes.size());

        if (campagnes.isEmpty()) {
            throw new RuntimeException("Aucune campagne active trouvée");
        }

        Campagne campagne = campagnes.get(0);
        System.out.println("=== Campagne ID : " + campagne.getId());

        // ── Upload Cloudinary ─────────────────────────────────────
        String url;
        try {
            url = cloudinaryService.uploadFile(file, "etats_rh");
            System.out.println("=== URL Cloudinary : " + url);
        } catch (Exception e) {
            throw new RuntimeException("Erreur Cloudinary : " + e.getMessage());
        }

        // ── Upsert ───────────────────────────────────────────────
        EtatRH etat = etatRHRepository
                .findByUtilisateurIdAndCampagneId(rh.getId(), campagne.getId())
                .orElse(new EtatRH());

        String snapshotAvant = etat.getId() != null
                ? etat.getStatut() + " | " + etat.getNomFichier()
                : null;

        etat.setUrl(url);
        etat.setNomFichier(file.getOriginalFilename());
        etat.setDateUpload(LocalDateTime.now());
        etat.setStatut(StatutEtat.SOUMIS);
        etat.setUtilisateur(rh);
        etat.setCampagne(campagne);
        etat.setRegion(rh.getRegion());

        EtatRH saved = etatRHRepository.save(etat);

        // 🆕 AUDIT
        String action = snapshotAvant == null ? "UPLOAD_ETAT" : "RE_UPLOAD_ETAT";
        auditLogService.log(email, action, "EtatRH",
                saved.getId(), snapshotAvant, saved, ip, "SUCCESS");

        return saved;
    }

    // ── RH_REGIONAL : voir son propre état ───────────────────────
    public Optional<EtatRHDTO> getMonEtat(String email, String ip) {

        Utilisateur rh = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Campagne campagne = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE)
                .stream().findFirst()
                .orElse(null);

        if (campagne == null) {
            auditLogService.log(email, "READ_MON_ETAT", "EtatRH",
                    null, null, "aucune campagne active", ip, "SUCCESS");
            return Optional.empty();
        }

        Optional<EtatRHDTO> result = etatRHRepository
                .findByUtilisateurIdAndCampagneId(rh.getId(), campagne.getId())
                .map(this::toDTO);

        // 🆕 AUDIT
        auditLogService.log(email, "READ_MON_ETAT", "EtatRH",
                campagne.getId(), null,
                result.isPresent() ? "état trouvé" : "aucun état",
                ip, "SUCCESS");

        return result;
    }

    // ── ADMIN : tous les états de la campagne active ──────────────
    public List<EtatRHDTO> getAllEtatsCampagneActive(String email, String ip) {

        Campagne campagne = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE)
                .stream().findFirst()
                .orElse(null);

        if (campagne == null) {
            auditLogService.log(email, "READ_ALL_ETATS", "EtatRH",
                    null, null, "aucune campagne active", ip, "SUCCESS");
            return List.of();
        }

        List<EtatRHDTO> result = etatRHRepository
                .findByCampagneId(campagne.getId())
                .stream().map(this::toDTO)
                .collect(Collectors.toList());

        // 🆕 AUDIT
        auditLogService.log(email, "READ_ALL_ETATS", "EtatRH",
                campagne.getId(), null, result.size() + " résultats", ip, "SUCCESS");

        return result;
    }

    // ── ADMIN : valider / rejeter ─────────────────────────────────
    public EtatRH changerStatut(Long etatId, StatutEtat statut, String email, String ip) {
        EtatRH etat = etatRHRepository.findById(etatId)
                .orElseThrow(() -> new RuntimeException("État introuvable"));

        String snapshotAvant = etat.getStatut().toString();

        etat.setStatut(statut);
        EtatRH saved = etatRHRepository.save(etat);

        // 🆕 AUDIT
        auditLogService.log(email, "CHANGER_STATUT", "EtatRH",
                etatId, snapshotAvant, statut, ip, "SUCCESS");

        return saved;
    }

    // ── Mapping ───────────────────────────────────────────────────
    private EtatRHDTO toDTO(EtatRH e) {
        return new EtatRHDTO(
                e.getId(),
                e.getUrl(),
                e.getNomFichier(),
                e.getStatut().name(),
                e.getDateUpload(),
                e.getRegion() != null ? e.getRegion().getNom() : "",
                e.getUtilisateur().getNom() + " " + e.getUtilisateur().getPrenom(),
                e.getCampagne().getId()
        );
    }
}