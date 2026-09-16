package dev.darshit.urlshortener.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final String userName;
    private final String password;
    private final String[] corsEnabled;

    public SecurityConfiguration(
            @Value("${USER_NAME}") String userName,
            @Value("${PASSWORD}") String password,
            @Value("${CORS_ENABLED}") String[] corsEnabled) {
        this.userName = requireValue("USER_NAME", userName);
        this.password = requireValue("PASSWORD", password);
        if (corsEnabled == null || corsEnabled.length == 0) {
            throw new IllegalStateException("CORS_ENABLED must contain at least one origin");
        }
        String[] normalizedOrigins = new String[corsEnabled.length];
        for (int index = 0; index < corsEnabled.length; index++) {
            String value = requireValue("CORS_ENABLED", corsEnabled[index]).trim();
            if ("*".equals(value)) {
                throw new IllegalStateException("Wildcard CORS origins are not allowed");
            }
            normalizedOrigins[index] = value;
        }
        this.corsEnabled = normalizedOrigins;
    }

    private static String requireValue(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must not be blank");
        }
        return value;
    }

    private static final String[] WHITELIST = {
            "/shorten",
            "/resolve/{shortPath}",
            "/ui/**",
            "/{shortPath}",
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/swagger-ui/**"
    };

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(userName)
                .password(passwordEncoder().encode(password))
                .roles("USER");
    }

    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(corsEnabled));
        configuration.setAllowedMethods(List.of("POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/shorten", configuration);
        return source;
    }
}
