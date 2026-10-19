package slari.researchers.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Security Headers
                .headers(headers -> headers
                                .contentSecurityPolicy(csp -> csp
                                        .policyDirectives("default-src 'self'; " +
                                                "script-src 'self' https://cdn.jsdelivr.net; " +
                                                "style-src 'self' https://cdn.jsdelivr.net; " +
                                                "img-src 'self' data: https://res.cloudinary.com; " +
                                                "font-src 'self' https://cdn.jsdelivr.net; " +
                                                "connect-src 'self'")
                                )
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        // Removed deprecated .xssProtection()
                )

                .authorizeHttpRequests(auth -> auth
                        // Swagger UI - PERMIT ALL
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // Public resources
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()

                        // Public auth endpoints
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",           // ✅ Added
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/validate"
                        ).permitAll()

                        // Browse varieties - PUBLIC
                        .requestMatchers(HttpMethod.GET, "/api/varieties/**").permitAll()

                        // Variety CRUD - RESEARCHER and ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/varieties")
                        .hasAnyRole("RESEARCHER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/varieties/bulk")  // ✅ Added bulk upload
                        .hasAnyRole("RESEARCHER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/varieties/**")
                        .hasAnyRole("RESEARCHER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/varieties/**")
                        .hasAnyRole("RESEARCHER", "ADMIN")

                        // Variety status management
                        .requestMatchers("/api/varieties/*/publish")
                        .hasAnyRole("RESEARCHER", "ADMIN")
                        .requestMatchers("/api/varieties/*/unpublish")
                        .hasAnyRole("RESEARCHER", "ADMIN")

                        // File uploads
                        .requestMatchers("/api/varieties/*/upload-image")
                        .hasAnyRole("RESEARCHER", "ADMIN")
                        .requestMatchers("/api/varieties/*/upload-document")
                        .hasAnyRole("RESEARCHER", "ADMIN")

                        // Admin only
                        .requestMatchers("/api/varieties/pending").hasRole("ADMIN")  // ✅ Added pending endpoint
                        .requestMatchers("/api/varieties/*/approve").hasRole("ADMIN")
                        .requestMatchers("/api/varieties/*/reject").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/varieties/**").hasRole("ADMIN")

                        // Recommendations - EXTENSION and ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/recommendations")
                        .hasAnyRole("EXTENSION", "ADMIN")
                        .requestMatchers("/api/recommendations/sent")
                        .hasAnyRole("EXTENSION", "ADMIN")
                        .requestMatchers("/api/recommendations/received")
                        .hasAnyRole("FARMER", "EXTENSION", "ADMIN")

                        // User management
                        .requestMatchers("/api/users/farmers").authenticated()
                        .requestMatchers("/api/users/researchers").authenticated()
                        .requestMatchers("/api/users/extension-officers").authenticated()  // ✅ Fixed
                        .requestMatchers("/api/users/by-role").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()

                        // Admin user management
                        .requestMatchers("/api/users").hasRole("ADMIN")                    // ✅ Added
                        .requestMatchers("/api/users/stats").hasRole("ADMIN")             // ✅ Added
                        .requestMatchers("/api/users/*/deactivate").hasRole("ADMIN")      // ✅ Added
                        .requestMatchers("/api/users/*/activate").hasRole("ADMIN")        // ✅ Added
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // Ratings - AUTHENTICATED
                        .requestMatchers("/api/varieties/*/rate").authenticated()

                        // Bookmarks - AUTHENTICATED
                        .requestMatchers("/api/bookmarks/**").authenticated()

                        // Auth - AUTHENTICATED
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/change-password").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/account").authenticated()

                        // Everything else - AUTHENTICATED
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:3000",
                "http://localhost:8080",
                //"https://slari-research-hub.com",
                // Production — replace with your actual Netlify URL
                "https://slari-research-hub.netlify.app",
                // Add your custom domain later if SLARI gets one
                "https://research.slari.gov.sl"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "X-Requested-With"
        ));

        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}