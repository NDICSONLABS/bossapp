package cm.ndicsonlabs.bossapp.config;

import cm.ndicsonlabs.bossapp.service.DatabaseUserDetailsService;
import cm.ndicsonlabs.bossapp.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsService databaseUserDetailsService;

    SecurityConfig(UserDetailsService databaseUserDetailsService) {
        this.databaseUserDetailsService = databaseUserDetailsService;
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // === THIS IS THE IMPORTANT PART ===
                .headers(headers -> headers
                                // Option 1: Quickest for local development (recommended while testing)
//                                .frameOptions(frame -> frame.disable())

                                // Option 2: More secure (only allow same origin or specific sites)
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/styles.css").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/**/*.css").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/images/*.png").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/**/*.pdf").permitAll();
                });

        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    protected UserDetailsService userDetailsService() {
        return databaseUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}