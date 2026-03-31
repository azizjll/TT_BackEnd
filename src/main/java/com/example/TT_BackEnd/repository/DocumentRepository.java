package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findTopByTypeOrderByIdDesc(String type);
    List<Document> findByCandidatureSaisonnierId(Long saisonnierId);
}
