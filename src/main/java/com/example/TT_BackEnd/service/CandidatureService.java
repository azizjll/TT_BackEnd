package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import com.example.TT_BackEnd.util.EmailServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ParentAutoriseRepository parentRepo;


    public CandidatureService(CandidatureRepository candidatureRepo,
                              CampagneRepository campagneRepo,
                              SaisonnierRepository saisonnierRepo,
                              DocumentRepository documentRepo,
                              CloudinaryService cloudinaryService, RegionRepository regionRepo, StructureRepository structureRepo, AffectationRepository affectationRepo, UtilisateurRepository utilisateurRepo, PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepo, EmailServiceImpl emailService, ParentAutoriseRepository parentRepo) {
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
        this.parentRepo = parentRepo;
    }

    @Transactional
    public void deposerCandidature(String nom, String prenom, Integer cin,
                                   String rib, String telephone, String email,
                                   String nomPrenomParent, String matriculeParent,
                                   String niveauEtude, String diplomeNom,
                                   String specialiteDiplome,
                                   String moisTravail,
                                   Long regionId, Long campagneId, Long structureId,
                                   MultipartFile cinFile, MultipartFile diplome,
                                   MultipartFile contrat, MultipartFile ribFile,
                                   boolean demandeAdminAutorisee,   // 🆕
                                   String messageDemandeAdmin,
                                   String rhEmail
    ) throws Exception {

        // ── 0. Vérifier parent autorisé ─────────────────────────────
        // ── 0. Vérifier parent autorisé ─────────────────────────────
        ParentAutorise parent = parentRepo
                .findByNomPrenomAndMatricule(nomPrenomParent.trim(), matriculeParent.trim())
                .orElseThrow(() -> new RuntimeException("Parent non autorisé ❌"));

        boolean depasse = parent.getUtilise() >= parent.getAutorises();

        if (depasse && !demandeAdminAutorisee) {
            throw new RuntimeException("QUOTA_DEPASSE"); // code spécial intercepté côté frontend
        }

        // ── 1. Vérifier ou créer Saisonnier ─────────────────────────
        Saisonnier s = saisonnierRepo.findByCin(cin).orElse(null);

        if (s == null) {
            s = new Saisonnier();
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
            s.setMoisTravail(moisTravail);

            s.setRegion(regionRepo.findById(regionId)
                    .orElseThrow(() -> new RuntimeException("Région non trouvée")));

            saisonnierRepo.save(s);

        } else {
            s.setTelephone(telephone);
            s.setEmail(email);
            saisonnierRepo.save(s);
        }

        // ── 2. Empêcher double candidature même campagne ───────────
        boolean dejaPostule = candidatureRepo
                .existsBySaisonnierIdAndCampagneId(s.getId(), campagneId);

        if (dejaPostule) {
            throw new RuntimeException("Vous avez déjà postulé à cette campagne ❌");
        }

        // ── 3. Créer utilisateur si n'existe pas ───────────────────
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
            utilisateur.setEnabled(false);
            utilisateur.setRegion(s.getRegion());
            utilisateur.setSaisonnier(s);

            utilisateurRepo.save(utilisateur);

            VerificationToken token = new VerificationToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(utilisateur);
            token.setExpiryDate(LocalDateTime.now().plusDays(1));

            verificationTokenRepo.save(token);

            emailService.sendSaisonnierWelcomeEmail(
                    email,
                    prenom + " " + nom,
                    motDePasseTemp,
                    token.getToken()
            );
        }

        // ── 4. Créer candidature ───────────────────────────────────
        Candidature c = new Candidature();
        c.setDateDepot(LocalDate.now());

// 🆕 Si quota dépassé → EN_ATTENTE_VALIDATION_ADMIN
        if (depasse) {
            c.setStatut(StatutCandidature.EN_ATTENTE_VALIDATION_ADMIN);
            c.setCommentaire(messageDemandeAdmin);
        } else {
            c.setStatut(StatutCandidature.EN_ATTENTE);
        }

        c.setCampagne(campagneRepo.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée")));
        c.setSaisonnier(s);
        candidatureRepo.save(c);

        // ── 5. Vérifier quota structure ────────────────────────────
        Structure structure = structureRepo.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        long nbCandidatures = affectationRepo
                .countByStructureIdAndCampagneId(structureId, campagneId);

        if (nbCandidatures >= structure.getAutorises()) {
            throw new RuntimeException("Quota atteint (" + structure.getAutorises() + ")");
        }

        // ── 6. Affectation ─────────────────────────────────────────
        Affectation affectation = new Affectation();
        affectation.setStructure(structure);
        affectation.setCampagne(c.getCampagne());
        affectation.setSaisonnier(s);
        affectation.setDateAffectation(LocalDate.now());

        structure.setRecrutes(structure.getRecrutes() + 1);

        affectationRepo.save(affectation);

        // ── 7. Incrémenter utilise ─────────────────────────────────
        parent.setUtilise(parent.getUtilise() + 1);
        parentRepo.save(parent);

// ── 8. Si quota dépassé → email spécifique aux admins ─────
        if (depasse) {
            List<String> emailsAdmins = utilisateurRepo.findByRole(RoleType.ADMIN)
                    .stream().map(Utilisateur::getEmail).collect(Collectors.toList());

            // récupérer le RH depuis son email
            Utilisateur rh = utilisateurRepo.findByEmail(rhEmail)
                    .orElse(null);

            String prenomRH   = rh != null ? rh.getPrenom() : "Inconnu";
            String nomRH      = rh != null ? rh.getNom()    : "Inconnu";
            String directionRH = s.getRegion().getNom();

            emailService.envoyerDemandeAutorisationQuotaParent(
                    s.getPrenom(),
                    s.getNom(),
                    s.getCin().toString(),
                    matriculeParent,
                    nomPrenomParent,
                    parent.getUtilise(),     // après incrément
                    parent.getAutorises(),
                    directionRH,
                    prenomRH,
                    nomRH,
                    messageDemandeAdmin,
                    emailsAdmins
            );
        }

        // ── 8. Upload documents ───────────────────────────────────
        saveDoc(c, cinFile, "CIN");
        saveDoc(c, diplome, "DIPLOME");
        saveDoc(c, contrat, "CONTRAT");
        saveDoc(c, ribFile, "RIB");
    }


    public Map<String, Object> getParentByMatricule(String matricule) {
        ParentAutorise parent = parentRepo.findByMatricule(matricule.trim())
                .orElseThrow(() -> new RuntimeException("Matricule introuvable"));

        boolean depasse = parent.getUtilise() >= parent.getAutorises();

        return Map.of(
                "nomPrenom",  parent.getNomPrenom(),
                "autorises",  parent.getAutorises(),
                "utilise",    parent.getUtilise(),
                "depasse",    depasse
        );
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
            String moisTravail,
            String statut,
            String commentaire,
            Long structureId,
            String nomPrenomParent, String matriculeParent,
            String niveauEtude, String diplome, String specialiteDiplome
    ) {

        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Saisonnier s = c.getSaisonnier();

        // ── update saisonnier ──
        s.setNom(nom);
        s.setPrenom(prenom);
        s.setCin(cin);
        s.setRib(rib);
        s.setTelephone(telephone);
        s.setEmail(email);
        s.setRegion(regionRepo.findById(regionId).get());

        if (moisTravail != null && !moisTravail.isBlank()) {
            s.setMoisTravail(moisTravail);
        }


        if (nomPrenomParent   != null && !nomPrenomParent.isBlank())   s.setNomPrenomParent(nomPrenomParent);
        if (matriculeParent   != null && !matriculeParent.isBlank())   s.setMatriculeParent(matriculeParent);
        if (niveauEtude       != null && !niveauEtude.isBlank())       s.setNiveauEtude(niveauEtude);
        if (diplome           != null && !diplome.isBlank())           s.setDiplome(diplome);
        if (specialiteDiplome != null && !specialiteDiplome.isBlank()) s.setSpecialiteDiplome(specialiteDiplome);



        saisonnierRepo.save(s);

        // ── update candidature ──
        c.setStatut(StatutCandidature.valueOf(statut));
        c.setCommentaire(commentaire);

        // 🔥 GESTION STRUCTURE
        if (structureId != null) {

            // ancienne affectation
            Affectation ancienne = affectationRepo
                    .findTopBySaisonnierIdOrderByDateAffectationDesc(s.getId())
                    .orElse(null);

            if (ancienne != null) {
                Structure oldStructure = ancienne.getStructure();
                oldStructure.setRecrutes(oldStructure.getRecrutes() - 1);
                affectationRepo.delete(ancienne);
            }

            // nouvelle structure
            Structure newStructure = structureRepo.findById(structureId)
                    .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

            // vérifier quota
            long nb = affectationRepo.countByStructureIdAndCampagneId(
                    structureId,
                    c.getCampagne().getId()
            );

            if (nb >= newStructure.getAutorises()) {
                throw new RuntimeException("Quota atteint ❌");
            }

            Affectation newAffectation = new Affectation();
            newAffectation.setStructure(newStructure);
            newAffectation.setCampagne(c.getCampagne());
            newAffectation.setSaisonnier(s);
            newAffectation.setDateAffectation(LocalDate.now());

            newStructure.setRecrutes(newStructure.getRecrutes() + 1);

            affectationRepo.save(newAffectation);
        }

        return candidatureRepo.save(c);
    }

    public void envoyerDemandeJuilletAout(Long candidatureId, String commentaire) {

        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Saisonnier s = c.getSaisonnier();

        // ── Changer le statut ──
        c.setStatut(StatutCandidature.EN_ATTENTE_VALIDATION_ADMIN);
        c.setCommentaire(commentaire);
        candidatureRepo.save(c);  // ← sauvegarder le nouveau statut

        // ── Envoyer l'email ──
        String directionNom = s.getRegion().getNom();

        List<String> emailsAdmins = utilisateurRepo.findByRole(RoleType.ADMIN)
                .stream()
                .map(Utilisateur::getEmail)
                .collect(Collectors.toList());

        if (emailsAdmins.isEmpty()) {
            throw new RuntimeException("Aucun administrateur trouvé");
        }

        emailService.envoyerDemandeAutorisationJuilletAout(
                s.getPrenom(),
                s.getNom(),
                s.getCin().toString(),
                directionNom,
                commentaire,
                emailsAdmins
        );
    }



    public List<Document> getDocumentsBySaisonnier(Long saisonnierId) {
        return documentRepo.findByCandidatureSaisonnierId(saisonnierId);
    }

    public Saisonnier getSaisonnierById(Long id) {
        return saisonnierRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Saisonnier non trouvé"));
    }

    public List<Candidature> getHistoriqueCandidatures(String email) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Saisonnier saisonnier = utilisateur.getSaisonnier();
        if (saisonnier == null) {
            return List.of(); // pas encore de candidature
        }

        return candidatureRepo.findBySaisonnierId(saisonnier.getId());
    }

    public void uploadParentsExcel(MultipartFile file) throws Exception {

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String nomPrenom  = row.getCell(0).getStringCellValue().trim();
            String matricule  = getCellValueAsString(row.getCell(1)).trim();
            int    autorises  = (int) row.getCell(2).getNumericCellValue(); // 🆕 colonne C

            if (parentRepo.existsByMatricule(matricule)) continue;

            ParentAutorise parent = new ParentAutorise();
            parent.setNomPrenom(nomPrenom);
            parent.setMatricule(matricule);
            parent.setAutorises(autorises);
            parent.setUtilise(0);

            parentRepo.save(parent);
        }

        workbook.close();
    }


    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Évite le format "12345.0"
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

        public List<Document> getDocumentsByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Saisonnier saisonnier = utilisateur.getSaisonnier();
        if (saisonnier == null) return List.of();

        return documentRepo.findByCandidatureSaisonnierId(saisonnier.getId());
    }

    public Map<String, Object> getProfilByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Saisonnier s = utilisateur.getSaisonnier();
        if (s == null) throw new RuntimeException("Profil saisonnier non trouvé");

        // ── Récupérer la dernière affectation ──────────────────
        Affectation derniereAffectation = affectationRepo
                .findTopBySaisonnierIdOrderByDateAffectationDesc(s.getId())
                .orElse(null);

        Map<String, Object> profil = new java.util.LinkedHashMap<>();
        profil.put("nom",               s.getNom());
        profil.put("prenom",            s.getPrenom());
        profil.put("email",             s.getEmail());
        profil.put("telephone",         s.getTelephone());
        profil.put("cin",               s.getCin());
        profil.put("rib",               s.getRib());
        profil.put("region",            s.getRegion() != null ? Map.of(
                "id",  s.getRegion().getId(),
                "nom", s.getRegion().getNom()
        ) : null);
        profil.put("nomPrenomParent",   s.getNomPrenomParent());
        profil.put("matriculeParent",   s.getMatriculeParent());
        profil.put("niveauEtude",       s.getNiveauEtude());
        profil.put("diplome",           s.getDiplome());
        profil.put("specialiteDiplome", s.getSpecialiteDiplome());

        // ── Ajouter la structure ───────────────────────────────
        if (derniereAffectation != null && derniereAffectation.getStructure() != null) {
            Structure st = derniereAffectation.getStructure();
            profil.put("structure", Map.of(
                    "id",   st.getId(),
                    "nom",  st.getNom(),
                    "type", st.getType().toString(),
                    "adresse", st.getAdresse() != null ? st.getAdresse() : ""
            ));
        } else {
            profil.put("structure", null);
        }

        return profil;
    }

    public Map<String, Object> getStructureByCandidatureId(Long candidatureId) {
        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Affectation affectation = affectationRepo
                .findTopBySaisonnierIdOrderByDateAffectationDesc(c.getSaisonnier().getId())
                .orElse(null);

        if (affectation == null || affectation.getStructure() == null) {
            return Map.of("structure", "");
        }

        Structure st = affectation.getStructure();
        return Map.of(
                "id",      st.getId(),
                "nom",     st.getNom(),
                "type",    st.getType().toString(),
                "adresse", st.getAdresse() != null ? st.getAdresse() : ""
        );
    }

}