package com.evmaster.config;

import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    //PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // USER DETAILS SERVICE
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {

            // username = email
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())
                    )
            );
        };
    }

    //  AUTH MANAGER
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // SECURITY FILTER
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // ---------- PUBLIC ----------
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/api/auth/**",
                                "/uploads/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ---------- COMMUNITY READ ONLY ----------
                        .requestMatchers(HttpMethod.GET, "/community/posts").permitAll()

                        // ---------- LOGGED USER API ----------
                        .requestMatchers("/api/user/**").authenticated()

                        // ---------- DASHBOARDS ----------
                        .requestMatchers("/dashboard-owner.html").hasRole("EV_OWNER")
                        .requestMatchers("/dashboard-manager.html").hasRole("STATION_MANAGER")
                        .requestMatchers("/dashboard-admin.html").hasRole("SUPER_ADMIN")

                        // ---------- MANAGER API ----------
                        .requestMatchers(HttpMethod.POST, "/api/manager/bookings/*/finish")
                        .hasRole("STATION_MANAGER")

                        // ---------- EVERYTHING ELSE NEED LOGIN ----------
                        .anyRequest().authenticated()
                )

                // ================= LOGIN =================
                .formLogin(form -> form
                        .loginPage("/index.html")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")

                        .successHandler((request, response, authentication) -> {

                            String role = authentication.getAuthorities().iterator().next().getAuthority();

                            if ("ROLE_EV_OWNER".equals(role)) {
                                response.sendRedirect("/dashboard-owner.html");
                            } else if ("ROLE_STATION_MANAGER".equals(role)) {
                                response.sendRedirect("/dashboard-manager.html");
                            } else if ("ROLE_SUPER_ADMIN".equals(role)) {
                                response.sendRedirect("/dashboard-admin.html");
                            } else {
                                response.sendRedirect("/index.html?error=role");
                            }
                        })

                        .failureUrl("/index.html?error=true")
                        .permitAll()
                )

                //LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/index.html?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}
