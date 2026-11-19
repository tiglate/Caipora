package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.util.UserRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;


@Configuration
@EnableMethodSecurity
public class WebFormsSecurityConfig {

    public static final String REMEMBER_ME_COOKIE = "remember-me";
    private static final int MIN_REMEMBER_ME_KEY_LENGTH = 32;
    private static final Logger log = LoggerFactory.getLogger(WebFormsSecurityConfig.class);

    /**
     * Content Security Policy. Scripts may only be loaded from this origin (no inline scripts, no eval), which
     * neutralizes most XSS payloads. Inline styles are still needed by the rich-text editor (TinyMCE).
     */
    private static final String CONTENT_SECURITY_POLICY = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline'",
            "img-src 'self' data: blob:",
            "font-src 'self' data:",
            "connect-src 'self'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'");

    @Bean
    public PasswordEncoder passwordEncoder() {
        // creates hashes with {bcrypt} prefix
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain webFormsFilterChain(final HttpSecurity http,
            final UserDetailsService userDetailsService,
            @Value("${webForms.rememberMeKey:}") final String rememberMeKey,
            @Value("${webForms.rememberMeValidity:P14D}") final Duration rememberMeValidity) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers(EndpointRequest.toAnyEndpoint()))
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(EndpointRequest.to("health")).hasAnyAuthority(UserRoles.ROLE_ACTUATOR_ACCESS)
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).denyAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasAnyAuthority(UserRoles.ROLE_SWAGGER_ACCESS)
                    // public resources
                    .requestMatchers("/login", "/error", "/favicon.ico").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    // everything else requires an authenticated user; fine-grained checks are done with @PreAuthorize
                    .anyRequest().authenticated())
                .formLogin(form -> form
                    .loginPage("/login")
                    .usernameParameter("name")
                    .failureUrl("/login?loginError=true"))
                .rememberMe(rememberMe -> rememberMe
                    .userDetailsService(userDetailsService)
                    .tokenValiditySeconds((int) rememberMeValidity.toSeconds())
                    .rememberMeParameter("rememberMe")
                    .rememberMeCookieName(REMEMBER_ME_COOKIE)
                    .key(resolveRememberMeKey(rememberMeKey)))
                .logout(logout -> logout
                    .logoutSuccessUrl("/login?logoutSuccess=true")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("SESSION", REMEMBER_ME_COOKIE))
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login?loginRequired=true")))
                .headers(headers -> headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                    .permissionsPolicyHeader(permissions -> permissions.policy("camera=(), microphone=(), geolocation=(), payment=()")))
                .build();
    }

    /**
     * The remember-me key signs the remember-me cookies, so it must be secret. When it is not configured, a random key
     * is generated: remember-me cookies then stop working after a restart, but no guessable key is ever used.
     */
    static String resolveRememberMeKey(final String configuredKey) {
        if (configuredKey != null && !configuredKey.isBlank()) {
            if (configuredKey.length() < MIN_REMEMBER_ME_KEY_LENGTH) {
                throw new IllegalStateException("webForms.rememberMeKey (env REMEMBER_ME_KEY) must have at least " +
                        MIN_REMEMBER_ME_KEY_LENGTH + " characters, e.g. the output of `openssl rand -base64 48`.");
            }
            return configuredKey;
        }
        log.warn("REMEMBER_ME_KEY is not set: using a random key; \"remember me\" logins will not survive a restart.");
        final byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
