package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.entity.StructureType;
import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelCampagneParser {

    private final RegionRepository regionRepository;
    private final StructureRepository structureRepository;

    public List<Region> extraireRegions(MultipartFile file) {
        Map<String, Region> regionsMap = new LinkedHashMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String regionNom = getCellString(row, 0);
                if (regionNom.isBlank()) continue;

                if (!regionsMap.containsKey(regionNom)) {
                    Region region = regionRepository.findByNom(regionNom)
                            .orElseGet(() -> regionRepository.save(new Region(regionNom)));
                    regionsMap.put(regionNom, region);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture Excel : " + e.getMessage(), e);
        }

        return new ArrayList<>(regionsMap.values());
    }

    // ← NOUVELLE MÉTHODE : extraire structures SANS les sauvegarder
    public List<Structure> extraireStructures(MultipartFile file) {
        Map<String, Region> regionsMap = new LinkedHashMap<>();
        List<Structure> structures = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String regionNom = getCellString(row, 0);
                if (regionNom.isBlank()) continue;

                if (!regionsMap.containsKey(regionNom)) {
                    Region region = regionRepository.findByNom(regionNom)
                            .orElseGet(() -> regionRepository.save(new Region(regionNom)));
                    regionsMap.put(regionNom, region);
                }

                String nomStructure = getCellString(row, 1);
                String typeRaw      = getCellString(row, 2);
                int autorises       = getCellInt(row, 3);

                if (!nomStructure.isBlank()) {
                    Structure s = new Structure();
                    s.setNom(nomStructure);
                    s.setAutorises(autorises);
                    s.setRecrutes(0);
                    s.setRegion(regionsMap.get(regionNom));

                    try {
                        s.setType(StructureType.valueOf(typeRaw.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        s.setType(StructureType.ESPACE_COMMERCIAL);
                    }

                    structures.add(s); // ← PAS de save ici
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture Excel : " + e.getMessage(), e);
        }

        return structures;
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default      -> "";
        };
    }

    // ✅ Nouvelle méthode pour lire les entiers (colonne Autorisés)
    private int getCellInt(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            System.out.println("⚠️ Cellule null à col=" + col + " row=" + row.getRowNum());
            return 0;
        }

        System.out.println("🔍 Row=" + row.getRowNum() + " Col=" + col
                + " CellType=" + cell.getCellType()
                + " Value=" + cell);

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING  -> {
                try { yield Integer.parseInt(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0; }
            }
            case FORMULA -> (int) cell.getNumericCellValue(); // ✅ cas formule Excel
            default -> 0;
        };
    }
}