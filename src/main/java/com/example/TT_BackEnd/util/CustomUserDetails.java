package com.example.TT_BackEnd.util; // ← même package que JwtUtils

import com.example.TT_BackEnd.entity.Utilisateur;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Utilisateur utilisateur;

    public CustomUserDetails(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getNom()    { return utilisateur.getNom(); }
    public String getPrenom() { return utilisateur.getPrenom(); }

    @Override
    public String getUsername() { return utilisateur.getEmail(); }

    @Override
    public String getPassword() { return utilisateur.getPassword(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(utilisateur.getRole().name()));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(utilisateur.getEnabled());
    }
}