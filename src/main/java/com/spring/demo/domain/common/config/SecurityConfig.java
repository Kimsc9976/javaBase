package com.spring.demo.domain.common.config;

import com.spring.demo.domain.member.service.CustomUserDetailsService;
import com.spring.demo.global.jwt.service.TokenAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.spring.demo.global.jwt.service.TokenProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;


    public SecurityConfig(TokenProvider tokenProvider, CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }



//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Filter
        http
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/api/member/signin").permitAll()
                .requestMatchers("/api/member/inform").hasRole("USER")
                .anyRequest().permitAll()
            )
            .addFilterBefore(new TokenAuthFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        // 기본 제거
        http.httpBasic(withDefaults());

        // cors header access
//        http.cors().configurationSource(corsConfigurationSource());

        // csrf disable
        http.csrf(AbstractHttpConfigurer::disable);

        // cors disable
//        http.cors(cors -> cors.disable());

        // STATELESS
        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 권한설정
//        http.authorizeRequests(request -> {
//            request
//                    .requestMatchers(new AntPathRequestMatcher("/api/controller/**")).permitAll()
//                    .anyRequest().permitAll();
//
//        });
        return http.build();
    }
}
