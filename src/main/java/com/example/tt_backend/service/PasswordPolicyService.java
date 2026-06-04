package com.example.tt_backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT     = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL   = Pattern.compile("[^a-zA-Z0-9]");

    public void validate(String password) {
        List<String> violations = new ArrayList<>();
        if (password == null || password.length() < 12)
            violations.add("Au moins 12 caractères requis");
        if (password != null && !UPPERCASE.matcher(password).find())
            violations.add("Au moins une majuscule requise");
        if (password != null && !DIGIT.matcher(password).find())
            violations.add("Au moins un chiffre requis");
        if (password != null && !SPECIAL.matcher(password).find())
            violations.add("Au moins un caractère spécial requis");
        if (!violations.isEmpty())
            throw new WeakPasswordException(violations);
    }

    // ── Génération mot de passe fort — utilisable partout ──────────────
    public String generateTempPassword() {
        String upper   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower   = "abcdefghijklmnopqrstuvwxyz";
        String digits  = "0123456789";
        String special = "@#$%&*!?";
        String all     = upper + lower + digits + special;

        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        sb.append(upper.charAt(rng.nextInt(upper.length())));
        sb.append(digits.charAt(rng.nextInt(digits.length())));
        sb.append(special.charAt(rng.nextInt(special.length())));

        for (int i = 3; i < 14; i++)
            sb.append(all.charAt(rng.nextInt(all.length())));

        List<Character> chars = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) chars.add(c);
        Collections.shuffle(chars, rng);

        StringBuilder result = new StringBuilder();
        chars.forEach(result::append);
        return result.toString();
    }

    public static class WeakPasswordException extends RuntimeException {
        private final List<String> violations;
        public WeakPasswordException(List<String> v) {
            super("Mot de passe trop faible");
            this.violations = v;
        }
        public List<String> getViolations() { return violations; }
    }
}