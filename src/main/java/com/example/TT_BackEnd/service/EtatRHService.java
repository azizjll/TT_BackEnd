package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.dto.EtatRHDTO;
import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.CampagneRepository;
import com.example.TT_BackEnd.repository.EtatRHRepository;
import com.example.TT_BackEnd.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CloudinaryService cloudinaryService; // votre service upload

    // ── RH_REGIONAL : uploader son état ──────────────────────────
    public EtatRH uploadEtat(MultipartFile file) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

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

        etat.setUrl(url);
        etat.setNomFichier(file.getOriginalFilename());
        etat.setDateUpload(LocalDateTime.now());
        etat.setStatut(StatutEtat.SOUMIS);
        etat.setUtilisateur(rh);
        etat.setCampagne(campagne);
        etat.setRegion(rh.getRegion());

        return etatRHRepository.save(etat);
    }
    // ── RH_REGIONAL : voir son propre état ───────────────────────
    public Optional<EtatRHDTO> getMonEtat() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Utilisateur rh = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Campagne campagne = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE)
                .stream().findFirst()
                .orElse(null);

        if (campagne == null) return Optional.empty();

        return etatRHRepository
                .findByUtilisateurIdAndCampagneId(rh.getId(), campagne.getId())
                .map(this::toDTO);
    }

    // ── ADMIN : tous les états de la campagne active ──────────────
    public List<EtatRHDTO> getAllEtatsCampagneActive() {

        Campagne campagne = campagneRepository
                .findByStatut(StatutCampagne.ACTIVE)
                .stream().findFirst()
                .orElse(null);

        if (campagne == null) return List.of();

        return etatRHRepository
                .findByCampagneId(campagne.getId())
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── ADMIN : valider / rejeter ─────────────────────────────────
    public EtatRH changerStatut(Long etatId, StatutEtat statut) {
        EtatRH etat = etatRHRepository.findById(etatId)
                .orElseThrow(() -> new RuntimeException("État introuvable"));
        etat.setStatut(statut);
        return etatRHRepository.save(etat);
    }

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