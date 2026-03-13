package com.example.TT_BackEnd.config;

import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.Structure;
import com.example.TT_BackEnd.entity.StructureType;
import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.repository.StructureRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class StructureDataLoader {

   /* @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Override
    public void run(String... args) {

        if(structureRepository.count() == 0) {

            // ===== Tunis =====
            Region tunis = regionRepository.findByNom("Gouvernorat de Tunis").orElse(null);
            if(tunis != null) {
                structureRepository.save(new Structure(null,"Avenue Habib Bourguiba",StructureType.ESPACE_COMMERCIAL,tunis));
                structureRepository.save(new Structure(null,"Berges du Lac II",StructureType.ESPACE_COMMERCIAL,tunis));
                structureRepository.save(new Structure(null,"Montplaisir",StructureType.ESPACE_COMMERCIAL,tunis));
                structureRepository.save(new Structure(null,"Cité El Khadra",StructureType.ESPACE_COMMERCIAL,tunis));

                structureRepository.save(new Structure(null,"Siège Social (Jardins du Lac II)",StructureType.CENTRE_TECHNOLOGIQUE,tunis));
                structureRepository.save(new Structure(null,"Data Center National",StructureType.CENTRE_TECHNOLOGIQUE,tunis));
            }

            // ===== Ariana =====
            Region ariana = regionRepository.findByNom("Gouvernorat de l'Ariana").orElse(null);
            if(ariana != null) {
                structureRepository.save(new Structure(null,"Ariana Ville",StructureType.ESPACE_COMMERCIAL,ariana));
                structureRepository.save(new Structure(null,"Ennasr II",StructureType.ESPACE_COMMERCIAL,ariana));
                structureRepository.save(new Structure(null,"La Soukra",StructureType.ESPACE_COMMERCIAL,ariana));
                structureRepository.save(new Structure(null,"Menzah VI",StructureType.ESPACE_COMMERCIAL,ariana));

                structureRepository.save(new Structure(null,"Pôle de Compétitivité El Ghazala",StructureType.CENTRE_TECHNOLOGIQUE,ariana));
                structureRepository.save(new Structure(null,"Centre Technique Regional",StructureType.CENTRE_TECHNOLOGIQUE,ariana));
            }

            // ===== Ben Arous =====
            Region benArous = regionRepository.findByNom("Gouvernorat de Ben Arous").orElse(null);
            if(benArous != null) {
                structureRepository.save(new Structure(null,"Ben Arous Centre",StructureType.ESPACE_COMMERCIAL,benArous));
                structureRepository.save(new Structure(null,"Megrine",StructureType.ESPACE_COMMERCIAL,benArous));
                structureRepository.save(new Structure(null,"Hammam Lif",StructureType.ESPACE_COMMERCIAL,benArous));
                structureRepository.save(new Structure(null,"Azur City",StructureType.ESPACE_COMMERCIAL,benArous));

                structureRepository.save(new Structure(null,"Zone Industrielle Megrine",StructureType.CENTRE_TECHNOLOGIQUE,benArous));
                structureRepository.save(new Structure(null,"Fibre Optique High-Tech",StructureType.CENTRE_TECHNOLOGIQUE,benArous));
            }

            // ===== Manouba =====
            Region manouba = regionRepository.findByNom("Gouvernorat de Manouba").orElse(null);
            if(manouba != null) {
                structureRepository.save(new Structure(null,"Manouba Ville",StructureType.ESPACE_COMMERCIAL,manouba));
                structureRepository.save(new Structure(null,"Denden",StructureType.ESPACE_COMMERCIAL,manouba));
                structureRepository.save(new Structure(null,"Mornaguia",StructureType.ESPACE_COMMERCIAL,manouba));

                structureRepository.save(new Structure(null,"Campus Universitaire Manouba",StructureType.CENTRE_TECHNOLOGIQUE,manouba));
                structureRepository.save(new Structure(null,"Connectivité Fibre Universitaire",StructureType.CENTRE_TECHNOLOGIQUE,manouba));
            }

            // ===== Nabeul =====
            Region nabeul = regionRepository.findByNom("Gouvernorat de Nabeul").orElse(null);
            if(nabeul != null) {
                structureRepository.save(new Structure(null,"Nabeul Centre",StructureType.ESPACE_COMMERCIAL,nabeul));
                structureRepository.save(new Structure(null,"Hammamet",StructureType.ESPACE_COMMERCIAL,nabeul));
                structureRepository.save(new Structure(null,"Yasmine Hammamet",StructureType.ESPACE_COMMERCIAL,nabeul));
                structureRepository.save(new Structure(null,"Korba",StructureType.ESPACE_COMMERCIAL,nabeul));

                structureRepository.save(new Structure(null,"Cyberparc Nabeul",StructureType.CENTRE_TECHNOLOGIQUE,nabeul));
                structureRepository.save(new Structure(null,"Nœuds de raccordement fibre FTTH",StructureType.CENTRE_TECHNOLOGIQUE,nabeul));
            }

            // ===== Bizerte =====
            Region bizerte = regionRepository.findByNom("Gouvernorat de Bizerte").orElse(null);
            if(bizerte != null) {
                structureRepository.save(new Structure(null,"Bizerte Ville",StructureType.ESPACE_COMMERCIAL,bizerte));
                structureRepository.save(new Structure(null,"Menzel Bourguiba",StructureType.ESPACE_COMMERCIAL,bizerte));
                structureRepository.save(new Structure(null,"Zarzouna",StructureType.ESPACE_COMMERCIAL,bizerte));

                structureRepository.save(new Structure(null,"Cyberparc Bizerte",StructureType.CENTRE_TECHNOLOGIQUE,bizerte));
                structureRepository.save(new Structure(null,"Stations sous-marines de câbles",StructureType.CENTRE_TECHNOLOGIQUE,bizerte));
            }

            // ===== Sousse =====
            Region sousse = regionRepository.findByNom("Gouvernorat de Sousse").orElse(null);
            if(sousse != null) {
                structureRepository.save(new Structure(null,"Sousse Médina",StructureType.ESPACE_COMMERCIAL,sousse));
                structureRepository.save(new Structure(null,"Kantaoui",StructureType.ESPACE_COMMERCIAL,sousse));
                structureRepository.save(new Structure(null,"Sahloul",StructureType.ESPACE_COMMERCIAL,sousse));
                structureRepository.save(new Structure(null,"Akouda",StructureType.ESPACE_COMMERCIAL,sousse));

                structureRepository.save(new Structure(null,"Technopole de Sousse",StructureType.CENTRE_TECHNOLOGIQUE,sousse));
                structureRepository.save(new Structure(null,"Centre de Support Régional",StructureType.CENTRE_TECHNOLOGIQUE,sousse));
            }

            // ===== Monastir =====
            Region monastir = regionRepository.findByNom("Gouvernorat de Monastir").orElse(null);
            if(monastir != null) {
                structureRepository.save(new Structure(null,"Monastir Centre",StructureType.ESPACE_COMMERCIAL,monastir));
                structureRepository.save(new Structure(null,"Jemmel",StructureType.ESPACE_COMMERCIAL,monastir));
                structureRepository.save(new Structure(null,"Ksar Hellal",StructureType.ESPACE_COMMERCIAL,monastir));

                structureRepository.save(new Structure(null,"Cyberparc Monastir",StructureType.CENTRE_TECHNOLOGIQUE,monastir));
                structureRepository.save(new Structure(null,"Infrastructure de Cloud régional",StructureType.CENTRE_TECHNOLOGIQUE,monastir));
            }

            // ===== Mahdia =====
            Region mahdia = regionRepository.findByNom("Gouvernorat de Mahdia").orElse(null);
            if(mahdia != null) {
                structureRepository.save(new Structure(null,"Mahdia Ville",StructureType.ESPACE_COMMERCIAL,mahdia));
                structureRepository.save(new Structure(null,"Ksour Essef",StructureType.ESPACE_COMMERCIAL,mahdia));
                structureRepository.save(new Structure(null,"El Jem",StructureType.ESPACE_COMMERCIAL,mahdia));

                structureRepository.save(new Structure(null,"Direction Régionale Technique",StructureType.CENTRE_TECHNOLOGIQUE,mahdia));
            }

            // ===== Sfax =====
            Region sfax = regionRepository.findByNom("Gouvernorat de Sfax").orElse(null);
            if(sfax != null) {
                structureRepository.save(new Structure(null,"Sfax El Médina",StructureType.ESPACE_COMMERCIAL,sfax));
                structureRepository.save(new Structure(null,"Sakiet Ezzit",StructureType.ESPACE_COMMERCIAL,sfax));
                structureRepository.save(new Structure(null,"Sfax Jdid",StructureType.ESPACE_COMMERCIAL,sfax));

                structureRepository.save(new Structure(null,"Technopole de Sfax",StructureType.CENTRE_TECHNOLOGIQUE,sfax));
                structureRepository.save(new Structure(null,"Data Center Régional",StructureType.CENTRE_TECHNOLOGIQUE,sfax));
            }

            // ===== Kairouan =====
            Region kairouan = regionRepository.findByNom("Gouvernorat de Kairouan").orElse(null);
            if(kairouan != null) {
                structureRepository.save(new Structure(null,"Kairouan Ville",StructureType.ESPACE_COMMERCIAL,kairouan));
                structureRepository.save(new Structure(null,"Bouhajla",StructureType.ESPACE_COMMERCIAL,kairouan));
                structureRepository.save(new Structure(null,"Nasrallah",StructureType.ESPACE_COMMERCIAL,kairouan));

                structureRepository.save(new Structure(null,"Cyberparc Kairouan",StructureType.CENTRE_TECHNOLOGIQUE,kairouan));
                structureRepository.save(new Structure(null,"Relais de transmission Centre",StructureType.CENTRE_TECHNOLOGIQUE,kairouan));
            }

            // ===== Kasserine =====
            Region kasserine = regionRepository.findByNom("Gouvernorat de Kasserine").orElse(null);
            if(kasserine != null) {
                structureRepository.save(new Structure(null,"Kasserine Ville",StructureType.ESPACE_COMMERCIAL,kasserine));
                structureRepository.save(new Structure(null,"Thala",StructureType.ESPACE_COMMERCIAL,kasserine));
                structureRepository.save(new Structure(null,"Sbeitla",StructureType.ESPACE_COMMERCIAL,kasserine));

                structureRepository.save(new Structure(null,"Cyberparc Kasserine",StructureType.CENTRE_TECHNOLOGIQUE,kasserine));
                structureRepository.save(new Structure(null,"Infrastructures MobiRif",StructureType.CENTRE_TECHNOLOGIQUE,kasserine));
            }

            // ===== Sidi Bouzid =====
            Region sidiBouzid = regionRepository.findByNom("Gouvernorat de Sidi Bouzid").orElse(null);
            if(sidiBouzid != null) {
                structureRepository.save(new Structure(null,"Sidi Bouzid Centre",StructureType.ESPACE_COMMERCIAL,sidiBouzid));
                structureRepository.save(new Structure(null,"Regueb",StructureType.ESPACE_COMMERCIAL,sidiBouzid));
                structureRepository.save(new Structure(null,"Jilma",StructureType.ESPACE_COMMERCIAL,sidiBouzid));

                structureRepository.save(new Structure(null,"Cyberparc Sidi Bouzid",StructureType.CENTRE_TECHNOLOGIQUE,sidiBouzid));
                structureRepository.save(new Structure(null,"Extension Réseau 4G/5G",StructureType.CENTRE_TECHNOLOGIQUE,sidiBouzid));
            }

            // ===== Gafsa =====
            Region gafsa = regionRepository.findByNom("Gouvernorat de Gafsa").orElse(null);
            if(gafsa != null) {
                structureRepository.save(new Structure(null,"Gafsa Ville",StructureType.ESPACE_COMMERCIAL,gafsa));
                structureRepository.save(new Structure(null,"Metlaoui",StructureType.ESPACE_COMMERCIAL,gafsa));
                structureRepository.save(new Structure(null,"El Ksar",StructureType.ESPACE_COMMERCIAL,gafsa));

                structureRepository.save(new Structure(null,"Cyberparc Gafsa",StructureType.CENTRE_TECHNOLOGIQUE,gafsa));
                structureRepository.save(new Structure(null,"Hub maintenance technique Sud-Ouest",StructureType.CENTRE_TECHNOLOGIQUE,gafsa));
            }

            // ===== Tozeur =====
            Region tozeur = regionRepository.findByNom("Gouvernorat de Tozeur").orElse(null);
            if(tozeur != null) {
                structureRepository.save(new Structure(null,"Tozeur Centre",StructureType.ESPACE_COMMERCIAL,tozeur));
                structureRepository.save(new Structure(null,"Nefta",StructureType.ESPACE_COMMERCIAL,tozeur));

                structureRepository.save(new Structure(null,"Station transmission satellite",StructureType.CENTRE_TECHNOLOGIQUE,tozeur));
                structureRepository.save(new Structure(null,"Faisceaux hertziens",StructureType.CENTRE_TECHNOLOGIQUE,tozeur));
            }

            // ===== Kébili =====
            Region kebili = regionRepository.findByNom("Gouvernorat de Kébili").orElse(null);
            if(kebili != null) {
                structureRepository.save(new Structure(null,"Kébili Ville",StructureType.ESPACE_COMMERCIAL,kebili));
                structureRepository.save(new Structure(null,"Douz",StructureType.ESPACE_COMMERCIAL,kebili));

                structureRepository.save(new Structure(null,"Infrastructure réseau désertique",StructureType.CENTRE_TECHNOLOGIQUE,kebili));
                structureRepository.save(new Structure(null,"MobiRif",StructureType.CENTRE_TECHNOLOGIQUE,kebili));
            }

            // ===== Gabès =====
            Region gabes = regionRepository.findByNom("Gouvernorat de Gabès").orElse(null);
            if(gabes != null) {
                structureRepository.save(new Structure(null,"Gabès Centre",StructureType.ESPACE_COMMERCIAL,gabes));
                structureRepository.save(new Structure(null,"Mareth",StructureType.ESPACE_COMMERCIAL,gabes));
                structureRepository.save(new Structure(null,"El Hamma",StructureType.ESPACE_COMMERCIAL,gabes));

                structureRepository.save(new Structure(null,"Cyberparc Gabès",StructureType.CENTRE_TECHNOLOGIQUE,gabes));
                structureRepository.save(new Structure(null,"Centre Technique Portuaire",StructureType.CENTRE_TECHNOLOGIQUE,gabes));
            }

        }
    }*/
}