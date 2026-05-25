package com.example.TT_BackEnd.service;


import com.example.TT_BackEnd.entity.StatutCampagne;
import com.example.TT_BackEnd.entity.Utilisateur;
import com.example.TT_BackEnd.repository.UtilisateurRepository;
import com.example.TT_BackEnd.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        List<Utilisateur> users = userRepository.findAllByEmail(email);

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Utilisateur non trouvé : " + email);
        }

        Utilisateur utilisateur;

        if (users.size() > 1) {
            // Plusieurs comptes avec le même email → prendre celui de la campagne ACTIVE
            utilisateur = users.stream()
                    .filter(u -> u.getCampagne() != null
                            && u.getCampagne().getStatut() == StatutCampagne.ACTIVE)
                    .findFirst()
                    .orElse(users.get(0));
        } else {
            utilisateur = users.get(0);
        }

        return new CustomUserDetails(utilisateur);
    }
}
