package com.example.TT_BackEnd.service;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.entity.StructureType;
import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.repository.StructureRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ExcelReaderService {

    private final StructureRepository structureRepository;
    private final RegionRepository regionRepository;

    public ExcelReaderService(StructureRepository structureRepository,
                              RegionRepository regionRepository) {
        this.structureRepository = structureRepository;
        this.regionRepository = regionRepository;
    }

    public void importStructures(InputStream is) {
        try {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // ← NOUVEAU : collecter les noms du fichier Excel
            Set<String> nomsExcel = new HashSet<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String regionNom    = getCellString(row, 0);
                String structureNom = getCellString(row, 1);
                String type         = getCellString(row, 2).trim().toUpperCase();
                String adresse      = getCellString(row, 3);
                int autorises       = getCellInt(row, 4);
                int recrutes        = getCellInt(row, 5);

                if (regionNom.isBlank() || structureNom.isBlank()) continue;

                Region region = regionRepository.findByNom(regionNom).orElse(null);
                if (region == null) {
                    System.out.println("⚠ Région non trouvée : " + regionNom);
                    continue;
                }

                nomsExcel.add(structureNom); // ← tracker les noms présents

                // ← chercher par NOM + RÉGION (pas juste nom)
                Structure structure = structureRepository
                        .findByNomAndRegion(structureNom, region)
                        .orElse(new Structure());

                structure.setNom(structureNom);
                structure.setAdresse(adresse);
                structure.setRegion(region);
                structure.setAutorises(autorises);
                structure.setRecrutes(recrutes);

                try {
                    structure.setType(StructureType.valueOf(type));
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Type invalide : " + type);
                    continue;
                }

                structureRepository.save(structure);
                System.out.println("✅ Sauvegardé : " + structureNom);
            }

            workbook.close();
            System.out.println("✅ Import terminé — " + nomsExcel.size() + " structures traitées");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String getCellString(Row row, int col) {
        if (row.getCell(col) == null) return "";
        return switch (row.getCell(col).getCellType()) {
            case STRING  -> row.getCell(col).getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) row.getCell(col).getNumericCellValue());
            default      -> "";
        };
    }

    private int getCellInt(Row row, int col) {
        if (row.getCell(col) == null) return 0;
        return switch (row.getCell(col).getCellType()) {
            case NUMERIC -> (int) row.getCell(col).getNumericCellValue();
            case STRING  -> {
                try { yield Integer.parseInt(row.getCell(col).getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0; }
            }
            default -> 0;
        };
    }
}