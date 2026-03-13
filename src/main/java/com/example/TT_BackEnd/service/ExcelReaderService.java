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
import java.util.Optional;

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

            int totalRows = sheet.getPhysicalNumberOfRows();
            System.out.println("Total de lignes (y compris header) : " + totalRows);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    System.out.println("Ignorer l'en-tête");
                    continue;
                }

                String regionNom = row.getCell(0).getStringCellValue().trim();
                String structureNom = row.getCell(1).getStringCellValue().trim();
                String type = row.getCell(2).getStringCellValue().trim();
                String adresse = row.getCell(3).getStringCellValue().trim();

                System.out.println("Ligne " + row.getRowNum() + " : "
                        + regionNom + " | " + structureNom + " | " + type + " | " + adresse);

                Region region = regionRepository.findByNom(regionNom).orElse(null);
                if (region == null) {
                    System.out.println("⚠ Région non trouvée : " + regionNom + " (ligne " + row.getRowNum() + ")");
                    continue;
                }

                try {
                    Optional<Structure> existingStructure = structureRepository.findByNom(structureNom);

                    if (existingStructure.isPresent()) {
                        // Mise à jour si la structure existe déjà
                        Structure structureToUpdate = existingStructure.get();
                        structureToUpdate.setAdresse(adresse);
                        structureToUpdate.setRegion(region);
                        structureToUpdate.setType(StructureType.valueOf(type));
                        structureRepository.save(structureToUpdate);
                        System.out.println("🔄 Mise à jour : " + structureNom + " (ligne " + row.getRowNum() + ")");
                    } else {
                        // Nouvelle insertion
                        Structure newStructure = new Structure();
                        newStructure.setNom(structureNom);
                        newStructure.setAdresse(adresse);
                        newStructure.setRegion(region);
                        newStructure.setType(StructureType.valueOf(type));
                        structureRepository.save(newStructure);
                        System.out.println("➕ Nouvelle insertion : " + structureNom + " (ligne " + row.getRowNum() + ")");
                    }

                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Type invalide pour Structure : " + type + " (ligne " + row.getRowNum() + ")");
                }
            }

            workbook.close();
            System.out.println("✅ Import terminé");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l'import Excel");
        }
    }
}