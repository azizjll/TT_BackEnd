package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

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

    public CandidatureService(CandidatureRepository candidatureRepo,
                              CampagneRepository campagneRepo,
                              SaisonnierRepository saisonnierRepo,
                              DocumentRepository documentRepo,
                              CloudinaryService cloudinaryService, RegionRepository regionRepo, StructureRepository structureRepo, AffectationRepository affectationRepo) {
        this.candidatureRepo = candidatureRepo;
        this.campagneRepo = campagneRepo;
        this.saisonnierRepo = saisonnierRepo;
        this.documentRepo = documentRepo;
        this.cloudinaryService = cloudinaryService;
        this.regionRepo = regionRepo;
        this.structureRepo = structureRepo;
        this.affectationRepo = affectationRepo;
    }

    @Transactional
    public void deposerCandidature(String nom, String prenom, Integer cin,
                                   String rib, String telephone, String email,
                                   Long regionId, Long campagneId, Long structureId,
                                   MultipartFile cinFile,
                                   MultipartFile diplome,
                                   MultipartFile contrat) throws Exception {

        // 1. Créer saisonnier
        Saisonnier s = new Saisonnier();
        s.setNom(nom);
        s.setPrenom(prenom);
        s.setCin(cin);
        s.setRib(rib);
        s.setTelephone(telephone);
        s.setEmail(email);
        s.setRegion(regionRepo.findById(regionId).get());

        saisonnierRepo.save(s);

        // 2. Créer candidature
        Candidature c = new Candidature();
        c.setDateDepot(LocalDate.now());
        c.setStatut(StatutCandidature.EN_ATTENTE);
        c.setCampagne(campagneRepo.findById(campagneId).get());
        c.setSaisonnier(s);

        candidatureRepo.save(c);

        Structure structure = structureRepo.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        // Utiliser autorises comme quota max
        long nbCandidatures = affectationRepo
                .countByStructureIdAndCampagneId(structureId, campagneId);

        if (nbCandidatures >= structure.getAutorises()) {
            throw new RuntimeException(
                    "Quota atteint pour cette structure ("
                            + structure.getAutorises() + " candidats max)"
            );
        }

        Affectation affectation = new Affectation();
        affectation.setStructure(structure);
        affectation.setCampagne(c.getCampagne());
        affectation.setSaisonnier(s);
        affectation.setDateAffectation(LocalDate.now());
        // Incrémenter recrutes automatiquement
        structure.setRecrutes(structure.getRecrutes() + 1);
        affectationRepo.save(affectation);

        // 3. Upload documents
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