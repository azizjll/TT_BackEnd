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
                                   MultipartFile contrat, MultipartFile ribFile) throws Exception {

        // ── 0. Vérifier parent autorisé ─────────────────────────────
        ParentAutorise parent = parentRepo
                .findByNomPrenomAndMatricule(nomPrenomParent.trim(), matriculeParent.trim())
                .orElseThrow(() -> new RuntimeException("Parent non autorisé ❌"));

        if (parent.isUtilise()) {
            throw new RuntimeException("Ce parent a déjà un candidat ❌");
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
        c.setStatut(StatutCandidature.EN_ATTENTE);
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

        // ── 7. Marquer parent comme utilisé 🔥 IMPORTANT ───────────
        parent.setUtilise(true);
        parentRepo.save(parent);

        // ── 8. Upload documents ───────────────────────────────────
        saveDoc(c, cinFile, "CIN");
        saveDoc(c, diplome, "DIPLOME");
        saveDoc(c, contrat, "CONTRAT");
        saveDoc(c, ribFile, "RIB");
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

            if (row.getRowNum() == 0) continue; // skip header

            // Colonne 0 : nom/prénom (toujours string)
            String nomPrenom = row.getCell(0).getStringCellValue().trim();

            // Colonne 1 : matricule (peut être number ou string)
            String matricule = getCellValueAsString(row.getCell(1)).trim();

            // Vérifier si matricule existe déjà
            if (parentRepo.existsByMatricule(matricule)) {
                continue; // skip les doublons
            }

            ParentAutorise parent = new ParentAutorise();
            parent.setNomPrenom(nomPrenom);
            parent.setMatricule(matricule);
            parent.setUtilise(false);

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