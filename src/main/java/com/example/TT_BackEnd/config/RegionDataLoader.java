package com.example.TT_BackEnd.config;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Component
public class RegionDataLoader implements CommandLineRunner {

    @Autowired
    private RegionRepository regionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (regionRepository.count() == 0) {
            List<String> gouvernorats = List.of(
                    "Gouvernorat de l'Ariana", "Gouvernorat de Béja", "Gouvernorat de Ben Arous",
                    "Gouvernorat de Bizerte", "Gouvernorat de Gabès", "Gouvernorat de Gafsa",
                    "Gouvernorat de Jendouba", "Gouvernorat de Kairouan", "Gouvernorat de Kasserine",
                    "Gouvernorat de Kébili", "Gouvernorat du Kef", "Gouvernorat de Mahdia",
                    "Gouvernorat de Manouba", "Gouvernorat de Médenine", "Gouvernorat de Monastir",
                    "Gouvernorat de Nabeul", "Gouvernorat de Sfax", "Gouvernorat de Sidi Bouzid",
                    "Gouvernorat de Siliana", "Gouvernorat de Sousse", "Gouvernorat de Tataouine",
                    "Gouvernorat de Tozeur", "Gouvernorat de Tunis", "Gouvernorat de Zaghouan"
            );
            gouvernorats.forEach(nom -> regionRepository.save(new Region(nom)));
        }
    }
}
