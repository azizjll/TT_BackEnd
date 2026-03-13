package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.service.ExcelReaderService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/structures")
@CrossOrigin("*")
public class StructureImportController {

    private final ExcelReaderService excelReaderService;

    public StructureImportController(ExcelReaderService excelReaderService) {
        this.excelReaderService = excelReaderService;
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importExcel(@RequestParam("file") MultipartFile file) {

        try {

            excelReaderService.importStructures(file.getInputStream());

            return "Import réussi";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'import";
        }
    }
}