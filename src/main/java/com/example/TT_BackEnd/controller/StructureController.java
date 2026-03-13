package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.repository.StructureRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/structures")
@CrossOrigin("*")
public class StructureController {

    private final StructureRepository structureRepository;

    public StructureController(StructureRepository structureRepository) {
        this.structureRepository = structureRepository;
    }

    @GetMapping("/region/{regionId}")
    public List<Structure> getStructuresByRegion(@PathVariable Long regionId) {

        return structureRepository.findByRegionId(regionId);
    }

}