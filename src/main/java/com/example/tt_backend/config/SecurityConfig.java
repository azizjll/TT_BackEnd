package com.example.tt_backend.config;

import com.example.tt_backend.service.UserDetailsServiceImpl;
import com.example.tt_backend.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Provider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }



    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // JWT Filter
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, userDetailsService);
    }

    // Security Filter Chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // ✅ En-têtes de sécurité
                .headers(headers -> headers

                        // CSP active (sans reportOnly)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data:; " +
                                                "font-src 'self'; " +
                                                "connect-src 'self'; " +
                                                "frame-ancestors 'none'; " +
                                                "form-action 'self';"
                                )
                        )

                        // ✅ Anti-Clickjacking
                        .frameOptions(frame -> frame.deny())

                        // ✅ X-Content-Type-Options
                        .contentTypeOptions(Customizer.withDefaults())
                )

                .authorizeHttpRequests(auth -> auth

                        // ✅ Routes publiques — uniquement ce qui est nécessaire
                        .requestMatchers(
                                "/auth/**",
                                "/api/campagnes/actives",
                                "/api/structures/campagne-active/publique",
                                "/api/candidatures/parent-by-matricule",
                                "/api/candidatures/depot",
                                "/files/**"


                        ).permitAll()

                        // ✅ Routes SUPERADMIN — réservées au rôle SUPERADMIN uniquement
                        .requestMatchers("/superadmin/**")
                        .hasAuthority("SUPERADMIN")

                        // ✅ Routes ADMIN
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/campagnes/**"

                        ).hasAnyAuthority("ADMIN", "SUPERADMIN","RH_REGIONAL")

                        // ✅ Routes utilisateurs authentifiés
                        .requestMatchers(

                                "/api/candidatures/**",
                                "/api/affectations/**",
                                "/api/saisonniers/**",
                                "/api/parents/**",
                                "/api/documents/**",
                                "/api/structures/**",
                                "/v3/api-docs/**",
                                "/structures/**",
                                "/swagger-ui/**",
                                "/api/parents/**",

                                "/swagger-ui.html"

                        ).authenticated()

                        // ✅ Tout le reste → authentification requise
                        .anyRequest().authenticated()
                )

                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://tt-front-end-amber.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // JWT Filter Class
    public static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtUtils jwtUtils;
        private final UserDetailsService userDetailsService;

        public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
            this.jwtUtils = jwtUtils;
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (jwtUtils.validateToken(token)) {
                    String email = jwtUtils.getUsernameFromToken(token);

                    var userDetails = userDetailsService.loadUserByUsername(email);

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        }
    }
}
