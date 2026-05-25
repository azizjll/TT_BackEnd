package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import com.example.TT_BackEnd.util.EmailServiceImpl;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    private final AuditLogService auditLogService;
    private final UtilisateurRepository utilisateurRepository;      // ✅ ajouter

    private final CandidatureRepository candidatureRepository;      // déjà présent ?


    public CandidatureService(CandidatureRepository candidatureRepo,
                              CampagneRepository campagneRepo,
                              SaisonnierRepository saisonnierRepo,
                              DocumentRepository documentRepo,
                              CloudinaryService cloudinaryService,
                              RegionRepository regionRepo,
                              StructureRepository structureRepo,
                              AffectationRepository affectationRepo,
                              UtilisateurRepository utilisateurRepo,
                              PasswordEncoder passwordEncoder,
                              VerificationTokenRepository verificationTokenRepo,
                              EmailServiceImpl emailService,
                              ParentAutoriseRepository parentRepo,
                              AuditLogService auditLogService, UtilisateurRepository utilisateurRepository, CandidatureRepository candidatureRepository) {
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
        this.auditLogService = auditLogService;
        this.utilisateurRepository = utilisateurRepository;
        this.candidatureRepository = candidatureRepository;
    }

    // ====================
    // DEPOT (inchangé)
    // ====================
    @Transactional
    public void deposerCandidature(String nom, String prenom, String cin,
                                   String rib, String telephone, String email,
                                   String nomPrenomParent, String matriculeParent,
                                   String niveauEtude, String diplomeNom,
                                   String specialiteDiplome,
                                   String moisTravail,
                                   Long regionId, Long campagneId, Long structureId,
                                   MultipartFile cinFile, MultipartFile diplome,
                                   MultipartFile ribFile,
                                   boolean demandeAdminAutorisee,
                                   String messageDemandeAdmin,
                                   String rhEmail
    ) throws Exception {

        // ── 0. Vérifier parent autorisé ─────────────────────────────
        ParentAutorise parent = parentRepo
                .findByNomPrenomAndMatriculeAndCampagneId(
                        nomPrenomParent.trim(),
                        matriculeParent.trim(),
                        campagneId)
                .orElseThrow(() -> new RuntimeException("Parent non autorisé ❌"));

        boolean depasse = parent.getUtilise() >= parent.getAutorises();

        if (depasse && !demandeAdminAutorisee) {
            throw new RuntimeException("Quota dépassé : Vous avez atteint le nombre maximal de matricules parents autorisés.");
        }

        // ── 1. Vérifier ou créer Saisonnier ─────────────────────────
        Saisonnier s = saisonnierRepo.findByCin(Integer.valueOf(cin)).orElse(null);

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
            utilisateur.setEnabled(true);
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

            Utilisateur rh = utilisateurRepo.findByEmail(rhEmail).orElse(null);

            String prenomRH    = rh != null ? rh.getPrenom() : "Inconnu";
            String nomRH       = rh != null ? rh.getNom()    : "Inconnu";
            String directionRH = s.getRegion().getNom();

            emailService.envoyerDemandeAutorisationQuotaParent(
                    s.getPrenom(),
                    s.getNom(),
                    s.getCin().toString(),
                    matriculeParent,
                    nomPrenomParent,
                    parent.getUtilise(),
                    parent.getAutorises(),
                    directionRH,
                    prenomRH,
                    nomRH,
                    messageDemandeAdmin,
                    emailsAdmins
            );
        }

        // ── 9. Upload documents ───────────────────────────────────
        saveDoc(c, cinFile, "CIN");
        saveDoc(c, diplome, "DIPLOME");
        saveDoc(c, ribFile, "RIB");
    }

    // ====================
    // READ
    // ====================
    public List<Candidature> getCandidaturesByRegion(Long regionId, String email, String ip) {
        List<Candidature> result = candidatureRepo.findBySaisonnierRegionId(regionId);

        auditLogService.log(email, "READ_BY_REGION", "Candidature",
                regionId, null, result.size() + " résultats", ip, "SUCCESS");

        return result;
    }

    public List<Candidature> getCandidaturesByCampagneAndRegion(Long campagneId, Long regionId, String email, String ip) {
        List<Candidature> result = candidatureRepo.findByCampagneIdAndSaisonnierRegionId(campagneId, regionId);

        auditLogService.log(email, "READ_FILTER", "Candidature",
                campagneId, null, result.size() + " résultats", ip, "SUCCESS");

        return result;
    }

    public List<Candidature> getAllCandidatures(String email, String ip) {
        List<Candidature> result = candidatureRepo.findAll();

        auditLogService.log(email, "READ_ALL", "Candidature",
                null, null, result.size() + " résultats", ip, "SUCCESS");

        return result;
    }

    public List<Document> getDocumentsBySaisonnier(Long saisonnierId, String email, String ip) {
        List<Document> docs = documentRepo.findByCandidatureSaisonnierId(saisonnierId);

        auditLogService.log(email, "READ_DOCUMENTS", "Document",
                saisonnierId, null, docs.size() + " documents", ip, "SUCCESS");

        return docs;
    }

    public Saisonnier getSaisonnierById(Long id, String email, String ip) {
        Saisonnier s = saisonnierRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Saisonnier non trouvé"));

        auditLogService.log(email, "READ", "Saisonnier",
                id, null, s, ip, "SUCCESS");

        return s;
    }

    public List<Candidature> getHistoriqueCandidatures(String email, String ip) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Saisonnier saisonnier = utilisateur.getSaisonnier();
        if (saisonnier == null) {
            auditLogService.log(email, "READ_HISTORIQUE", "Candidature",
                    null, null, "0 résultats", ip, "SUCCESS");
            return List.of();
        }

        List<Candidature> result = candidatureRepo.findBySaisonnierId(saisonnier.getId());

        auditLogService.log(email, "READ_HISTORIQUE", "Candidature",
                saisonnier.getId(), null, result.size() + " résultats", ip, "SUCCESS");

        return result;
    }

    public List<Document> getDocumentsByEmail(String email, String ip) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Saisonnier saisonnier = utilisateur.getSaisonnier();
        if (saisonnier == null) return List.of();

        List<Document> docs = documentRepo.findByCandidatureSaisonnierId(saisonnier.getId());

        auditLogService.log(email, "READ_MES_DOCUMENTS", "Document",
                saisonnier.getId(), null, docs.size() + " documents", ip, "SUCCESS");

        return docs;
    }

    public Map<String, Object> getProfilByEmail(String email, String ip) {
        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Saisonnier s = utilisateur.getSaisonnier();
        if (s == null) throw new RuntimeException("Profil saisonnier non trouvé");

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

        if (derniereAffectation != null && derniereAffectation.getStructure() != null) {
            Structure st = derniereAffectation.getStructure();
            profil.put("structure", Map.of(
                    "id",      st.getId(),
                    "nom",     st.getNom(),
                    "type",    st.getType().toString(),
                    "adresse", st.getAdresse() != null ? st.getAdresse() : ""
            ));
        } else {
            profil.put("structure", null);
        }

        auditLogService.log(email, "READ_PROFIL", "Saisonnier",
                s.getId(), null, "profil consulté", ip, "SUCCESS");

        return profil;
    }

    // CandidatureService.java
    public Map<String, Object> getParentByMatricule(String matricule, String email, String ip) {

        // ← récupérer la campagne active
        Campagne campagneActive = campagneRepo.findAll()
                .stream()
                .filter(c -> c.getStatut() == StatutCampagne.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune campagne active"));

        // ← chercher le parent dans cette campagne
        ParentAutorise parent = parentRepo
                .findByMatriculeAndCampagneId(matricule.trim(), campagneActive.getId())
                .orElseThrow(() -> new RuntimeException("Matricule introuvable"));

        boolean depasse = parent.getUtilise() >= parent.getAutorises();

        auditLogService.log(email, "READ_PARENT", "ParentAutorise",
                null, null, "matricule: " + matricule, ip, "SUCCESS");

        return Map.of(
                "nomPrenom", parent.getNomPrenom(),
                "autorises", parent.getAutorises(),
                "utilise",   parent.getUtilise(),
                "depasse",   depasse
        );
    }
    public Map<String, Object> getStructureByCandidatureId(Long candidatureId, String email, String ip) {
        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Affectation affectation = affectationRepo
                .findTopBySaisonnierIdOrderByDateAffectationDesc(c.getSaisonnier().getId())
                .orElse(null);

        auditLogService.log(email, "READ_STRUCTURE", "Candidature",
                candidatureId, null, "structure consultée", ip, "SUCCESS");

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

    // ====================
    // UPDATE
    // ====================
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
            String nomPrenomParent,
            String matriculeParent,
            String niveauEtude,
            String diplome,
            String specialiteDiplome,
            String userEmail,   // 🆕 audit
            String ip           // 🆕 audit
    ) {
        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // snapshot avant
        String snapshotAvant = c.getStatut() + " | " + c.getSaisonnier().getEmail();

        Saisonnier s = c.getSaisonnier();

        // ── update saisonnier ──
        s.setNom(nom);
        s.setPrenom(prenom);
        s.setCin(String.valueOf(cin));
        s.setRib(rib);
        s.setTelephone(telephone);
        s.setEmail(email);
        s.setRegion(regionRepo.findById(regionId).get());

        if (moisTravail != null && !moisTravail.isBlank())           s.setMoisTravail(moisTravail);
        if (nomPrenomParent != null && !nomPrenomParent.isBlank())   s.setNomPrenomParent(nomPrenomParent);
        if (matriculeParent != null && !matriculeParent.isBlank())   s.setMatriculeParent(matriculeParent);
        if (niveauEtude != null && !niveauEtude.isBlank())           s.setNiveauEtude(niveauEtude);
        if (diplome != null && !diplome.isBlank())                   s.setDiplome(diplome);
        if (specialiteDiplome != null && !specialiteDiplome.isBlank()) s.setSpecialiteDiplome(specialiteDiplome);

        saisonnierRepo.save(s);

        // ── update candidature ──
        StatutCandidature nouveauStatut = StatutCandidature.valueOf(statut);
        StatutCandidature ancienStatut  = c.getStatut();

        c.setStatut(nouveauStatut);
        c.setCommentaire(commentaire);

        // ── email si changement de statut ──
        if (ancienStatut != nouveauStatut) {
            String emailSaisonnier = s.getEmail();
            String prenomNom = s.getPrenom() + " " + s.getNom();
            try {
                if (nouveauStatut == StatutCandidature.ACCEPTEE) {
                    emailService.sendCandidatureAccepteeEmail(emailSaisonnier, prenomNom);
                } else if (nouveauStatut == StatutCandidature.REJETEE) {
                    emailService.sendCandidatureRefuseeEmail(emailSaisonnier, prenomNom);
                }
            } catch (Exception e) {
                log.error("❌ Échec envoi email statut candidature : {}", e.getMessage());
            }
        }

        // ── gestion structure ──
        if (structureId != null) {
            Affectation ancienne = affectationRepo
                    .findTopBySaisonnierIdOrderByDateAffectationDesc(s.getId())
                    .orElse(null);

            if (ancienne != null) {
                Structure oldStructure = ancienne.getStructure();
                oldStructure.setRecrutes(oldStructure.getRecrutes() - 1);
                affectationRepo.delete(ancienne);
            }

            Structure newStructure = structureRepo.findById(structureId)
                    .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

            long nb = affectationRepo.countByStructureIdAndCampagneId(
                    structureId, c.getCampagne().getId());

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

        Candidature saved = candidatureRepo.save(c);

        // 🆕 AUDIT
        auditLogService.log(userEmail, "UPDATE", "Candidature",
                candidatureId, snapshotAvant, saved, ip, "SUCCESS");

        return saved;
    }

    // ====================
    // LOGIQUE MÉTIER
    // ====================
    public void envoyerDemandeJuilletAout(Long candidatureId, String commentaire, String email, String ip) {
        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Saisonnier s = c.getSaisonnier();

        String ancienStatut = c.getStatut().toString();

        c.setStatut(StatutCandidature.EN_ATTENTE_VALIDATION_ADMIN);
        c.setCommentaire(commentaire);
        candidatureRepo.save(c);

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

        // 🆕 AUDIT
        auditLogService.log(email, "DEMANDE_AUTORISATION", "Candidature",
                candidatureId, ancienStatut, StatutCandidature.EN_ATTENTE_VALIDATION_ADMIN, ip, "SUCCESS");
    }

    public void uploadParentsExcel(MultipartFile file, Long campagneId, String email, String ip) throws Exception {

        // ← récupérer la campagne
        Campagne campagne = campagneRepo.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int nbInseres = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String matricule    = getCellValueAsString(row.getCell(1)).trim();
            String nomPrenom    = getCellValueAsString(row.getCell(2)).trim();
            String autorisesStr = getCellValueAsString(row.getCell(3)).trim();

            if (matricule.isEmpty() || nomPrenom.isEmpty()) continue;

            int autorises;
            try {
                autorises = Integer.parseInt(autorisesStr);
            } catch (NumberFormatException e) {
                continue;
            }

            // ← unicité par campagne, pas globale
            if (parentRepo.existsByMatriculeAndCampagneId(matricule, campagneId)) continue;

            ParentAutorise parent = new ParentAutorise();
            parent.setNomPrenom(nomPrenom);
            parent.setMatricule(matricule);
            parent.setAutorises(autorises);
            parent.setUtilise(0);
            parent.setCampagne(campagne);   // ← lier à la campagne

            parentRepo.save(parent);
            nbInseres++;
        }

        workbook.close();

        auditLogService.log(email, "UPLOAD_PARENTS_EXCEL", "ParentAutorise",
                campagneId, null, nbInseres + " parents insérés — fichier: " + file.getOriginalFilename(), ip, "SUCCESS");
    }
    // ====================
    // PRIVÉ
    // ====================
    private void saveDoc(Candidature c, MultipartFile file, String type) throws Exception {
        String url = cloudinaryService.uploadFile(file, "candidatures/" + c.getId());

        Document d = new Document();
        d.setNomFichier(file.getOriginalFilename());
        d.setType(type);
        d.setUrl(url);
        d.setCandidature(c);

        documentRepo.save(d);
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default:      return "";
        }
    }

    public List<Candidature> getCandidaturesParStructureResponsable(String email) {

        List<Utilisateur> users = utilisateurRepository.findAllByEmail(email);

        Utilisateur responsable;
        if (users.size() > 1) {
            responsable = users.stream()
                    .filter(u -> u.getCampagne() != null
                            && u.getCampagne().getStatut() == StatutCampagne.ACTIVE)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucune campagne active."));
        } else if (users.size() == 1) {
            responsable = users.get(0);
        } else {
            throw new RuntimeException("Utilisateur non trouvé.");
        }

        Structure structure = responsable.getStructure();
        Campagne campagne   = responsable.getCampagne();

        if (structure == null) throw new RuntimeException("Aucune structure affectée.");
        if (campagne == null)  throw new RuntimeException("Aucune campagne affectée.");

        return candidatureRepository.findByStructureAndCampagne(structure, campagne);
    }
}