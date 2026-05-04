package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.AnalyseIA;
import com.example.TT_BackEnd.entity.Candidature;
import com.example.TT_BackEnd.entity.Document;
import com.example.TT_BackEnd.entity.Saisonnier;
import com.example.TT_BackEnd.repository.AnalyseIARepository;
import com.example.TT_BackEnd.repository.CandidatureRepository;
import com.example.TT_BackEnd.repository.DocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyseIAService {

    private final CandidatureRepository candidatureRepo;
    private final DocumentRepository documentRepo;
    private final AnalyseIARepository analyseIARepo;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String FLASK_URL = "http://localhost:5001/analyser";

    public AnalyseIAService(CandidatureRepository candidatureRepo, DocumentRepository documentRepo, AnalyseIARepository analyseIARepo) {
        this.candidatureRepo = candidatureRepo;
        this.documentRepo = documentRepo;
        this.analyseIARepo = analyseIARepo;
    }

    public AnalyseIA analyserCandidature(Long candidatureId) {
        Candidature c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Saisonnier s = c.getSaisonnier();

        // ── 1. Construire donneesSaisies ──────────────────────
        Map<String, Object> donnees = Map.of(
                "nom",               s.getNom(),
                "prenom",            s.getPrenom(),
                "cin",               s.getCin().toString(),
                "rib",               s.getRib(),
                "diplomeNom",        s.getDiplome() != null ? s.getDiplome() : "",
                "specialiteDiplome", s.getSpecialiteDiplome() != null ? s.getSpecialiteDiplome() : ""
        );

        // ── 2. Récupérer URLs des documents ───────────────────
        List<Document> docs = documentRepo.findByCandidatureId(candidatureId);

        Map<String, String> urls = new HashMap<>();
        for (Document d : docs) {
            switch (d.getType()) {
                case "CIN"     -> urls.put("cinFile", d.getUrl());
                case "DIPLOME" -> urls.put("diplome", d.getUrl());
                case "RIB"     -> urls.put("ribFile", d.getUrl());
            }
        }

        // ── 3. Appeler Flask ──────────────────────────────────
        Map<String, Object> payload = Map.of(
                "donneesSaisies",  donnees,
                "urlsDocuments",   urls
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                FLASK_URL, payload, Map.class
        );

        Map<String, Object> resultat = response.getBody();

        // ── 4. Sauvegarder en BDD ─────────────────────────────
        AnalyseIA analyse = analyseIARepo
                .findByCandidatureId(candidatureId)
                .orElse(new AnalyseIA());  // mise à jour si déjà analysé

        analyse.setCandidature(c);
        analyse.setScore(((Number) resultat.get("score")).intValue());
        analyse.setDecision((String) resultat.get("decision"));
        analyse.setAlertes(resultat.get("alertes").toString());
        analyse.setResume((String) resultat.get("resume"));
        analyse.setAnalyseDate(LocalDateTime.now());

        return analyseIARepo.save(analyse);
    }
}