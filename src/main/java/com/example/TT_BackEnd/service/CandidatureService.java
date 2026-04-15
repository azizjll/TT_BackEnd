package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import com.example.TT_BackEnd.util.EmailServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CandidatureService {

    private final CandidatureRepository candidatureRepo;
    private final CampagneRepository campagneRepo;
    private final SaisonnierRepository saisonnierRepo;
    private final DocumentRepository documentRepo;
    private final CloudinaryService cloudinaryService;
    private final RegionRepository regionRepo;
    private final StructureRepository structureRepo;
    private final AffectationRepository affectationRepo;
    private final UtilisateurRepository utilisateurRepo;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepo;
    private final EmailServiceImpl emailService;

    public CandidatureService(CandidatureRepository candidatureRepo,
                              CampagneRepository campagneRepo,
                              SaisonnierRepository saisonnierRepo,
                              DocumentRepository documentRepo,
                              CloudinaryService cloudinaryService, RegionRepository regionRepo, StructureRepository structureRepo, AffectationRepository affectationRepo, UtilisateurRepository utilisateurRepo, PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepo, EmailServiceImpl emailService) {
        this.candidatureRepo = candidatureRepo;
        this.campagneRepo = campagneRepo;
        this.saisonnierRepo = saisonnierRepo;
        this.documentRepo = documentRepo;
        this.cloudinaryService = cloudinaryService;
        this.regionRepo = regionRepo;
        this.structureRepo = structureRepo;
        this.affectationRepo = affectationRepo;
        this.utilisateurRepo = utilisateurRepo;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepo = verificationTokenRepo;
        this.emailService = emailService;
    }

    @Transactional
    public void deposerCandidature(String nom, String prenom, Integer cin,
                                   String rib, String telephone, String email,
                                   String nomPrenomParent, String matriculeParent,
                                   String niveauEtude, String diplomeNom,
                                   String specialiteDiplome,
                                   Long regionId, Long campagneId, Long structureId,
                                   MultipartFile cinFile, MultipartFile diplome,
                                   MultipartFile contrat) throws Exception {

        // ── 1. Créer Saisonnier (entité métier) ──────────────────────
        Saisonnier s = new Saisonnier();
        s.setNom(nom);
        s.setPrenom(prenom);
        s.setCin(cin);
        s.setRib(rib);
        s.setTelephone(telephone);
        s.setEmail(email);
        s.setNomPrenomParent(nomPrenomParent);
        s.setMatriculeParent(matriculeParent);
        s.setNiveauEtude(niveauEtude);
        s.setDiplome(diplomeNom);
        s.setSpecialiteDiplome(specialiteDiplome);
        s.setRegion(regionRepo.findById(regionId)
                .orElseThrow(() -> new RuntimeException("Région non trouvée")));
        saisonnierRepo.save(s);

        // ── 2. Créer Utilisateur avec role SAISONNIER ─────────────────
        boolean utilisateurExiste = utilisateurRepo.findByEmail(email).isPresent();
        if (!utilisateurExiste) {
            String motDePasseTemp = UUID.randomUUID().toString().substring(0, 8);

            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            utilisateur.setTelephone(telephone);
            utilisateur.setRole(RoleType.SAISONNIER);
            utilisateur.setPassword(passwordEncoder.encode(motDePasseTemp));
            utilisateur.setEnabled(false); // activé après vérification email
            utilisateur.setRegion(s.getRegion());
            utilisateurRepo.save(utilisateur);

            // ── 3. Token de vérification ──────────────────────────────
            VerificationToken token = new VerificationToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(utilisateur);
            token.setExpiryDate(LocalDateTime.now().plusDays(1));
            verificationTokenRepo.save(token);

            // ── 4. Email avec mot de passe temporaire + lien vérif ────
            emailService.sendSaisonnierWelcomeEmail(
                    email,
                    prenom + " " + nom,
                    motDePasseTemp,
                    token.getToken()
            );
        }

        // ── 5. Créer Candidature ──────────────────────────────────────
        Candidature c = new Candidature();
        c.setDateDepot(LocalDate.now());
        c.setStatut(StatutCandidature.EN_ATTENTE);
        c.setCampagne(campagneRepo.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée")));
        c.setSaisonnier(s);
        candidatureRepo.save(c);

        // ── 6. Vérifier quota structure ───────────────────────────────
        Structure structure = structureRepo.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        long nbCandidatures = affectationRepo
                .countByStructureIdAndCampagneId(structureId, campagneId);

        if (nbCandidatures >= structure.getAutorises()) {
            throw new RuntimeException("Quota atteint pour cette structure ("
                    + structure.getAutorises() + " candidats max)");
        }

        Affectation affectation = new Affectation();
        affectation.setStructure(structure);
        affectation.setCampagne(c.getCampagne());
        affectation.setSaisonnier(s);
        affectation.setDateAffectation(LocalDate.now());
        structure.setRecrutes(structure.getRecrutes() + 1);
        affectationRepo.save(affectation);

        // ── 7. Upload documents ───────────────────────────────────────
        saveDoc(c, cinFile, "CIN");
        saveDoc(c, diplome, "DIPLOME");
        saveDoc(c, contrat, "CONTRAT");
    }
    private void saveDoc(Candidature c, MultipartFile file, String type) throws Exception {

        String url = cloudinaryService.uploadFile(file, "candidatures/" + c.getId());

        Document d = new Document();
        d.setNomFichier(file.getOriginalFilename());
        d.setType(type);
        d.setUrl(url);
        d.setCandidature(c);

        documentRepo.save(d);
    }

    public List<Candidature> getCandidaturesByRegion(Long regionId) {
        return candidatureRepo.findBySaisonnierRegionId(regionId);
    }

    public List<Candidature> getCandidaturesByCampagneAndRegion(Long campagneId, Long regionId) {
        return candidatureRepo.findByCampagneIdAndSaisonnierRegionId(campagneId, regionId);
    }

    public List<Candidature> getAllCandidatures() {
        return candidatureRepo.findAll();
    }


    @Transactional
    public Candidature updateCandidature(
            Long candidatureId,
            String nom,
            String prenom,
            Integer cin,
            String rib,
            String telephone,
            String email,
            Long regionId,
            String statut,
            String commentaire
    ) {

        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Saisonnier s = c.getSaisonnier();

        // modification saisonnier
        s.setNom(nom);
        s.setPrenom(prenom);
        s.setCin(cin);
        s.setRib(rib);
        s.setTelephone(telephone);
        s.setEmail(email);
        s.setRegion(regionRepo.findById(regionId).get());

        saisonnierRepo.save(s);

        // modification candidature
        c.setStatut(StatutCandidature.valueOf(statut));
        c.setCommentaire(commentaire);

        return candidatureRepo.save(c);
    }

    public List<Document> getDocumentsBySaisonnier(Long saisonnierId) {
        return documentRepo.findByCandidatureSaisonnierId(saisonnierId);
    }

    public Saisonnier getSaisonnierById(Long id) {
        return saisonnierRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Saisonnier non trouvé"));
    }

}