package com.example.lockly.config;

import com.example.lockly.constant.ApiPath;
import com.example.lockly.security.JwtAuthenticationFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

    JwtAuthenticationFilter jwtAuthenticationFilter;

    static final String[] PUBLIC_ENDPOINTS = {
            ApiPath.API_NOW + "/auth/register/send-otp",
            ApiPath.API_NOW + "/auth/register/verify-otp",
            ApiPath.API_NOW + "/auth/login",
            ApiPath.API_NOW + "/auth/forgot-password/send-otp",
            ApiPath.API_NOW + "/auth/forgot-password/verify-otp",  // ← thêm
            ApiPath.API_NOW + "/auth/forgot-password/reset",
            ApiPath.API_NOW + "/auth/logout",                       // ← thêm
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // [1] Tắt CSRF — REST API dùng JWT không cần CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // [2] Không dùng Session — stateless hoàn toàn
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // [3] Phân quyền endpoint
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // [4] Cắm JwtAuthenticationFilter vào trước filter mặc định của Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}