// repository/CallRapportRepository.java
package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.CallRapport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CallRapportRepository extends JpaRepository<CallRapport, Long> {
    List<CallRapport> findBySaisonnierTelephone(String telephone);
}