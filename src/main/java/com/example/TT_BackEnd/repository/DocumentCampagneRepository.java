package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.DocumentCampagne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentCampagneRepository extends JpaRepository<DocumentCampagne, Long> {

    List<DocumentCampagne> findByCampagneId(Long campagneId);
}