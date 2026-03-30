package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.entity.Document;
import com.example.TT_BackEnd.repository.DocumentRepository;
import com.example.TT_BackEnd.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin("*")
public class DocumentController {

    private final CloudinaryService cloudinaryService;
    private final DocumentRepository documentRepository;

    public DocumentController(CloudinaryService cloudinaryService,
                              DocumentRepository documentRepository) {
        this.cloudinaryService = cloudinaryService;
        this.documentRepository = documentRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type
    ) throws IOException {

        String url = cloudinaryService.uploadFile(file, "documents_tt");

        Document doc = new Document();
        doc.setNomFichier(file.getOriginalFilename());
        doc.setType(type); // ex: CIRCULAIRE_2025
        doc.setUrl(url);

        Document saved = documentRepository.save(doc);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Document> getByType(@PathVariable String type) {
        Document doc = documentRepository.findTopByTypeOrderByIdDesc(type);
        return ResponseEntity.ok(doc);
    }
}