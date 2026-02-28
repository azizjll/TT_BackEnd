package com.example.TT_BackEnd.repository;

import com.example.TT_BackEnd.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
