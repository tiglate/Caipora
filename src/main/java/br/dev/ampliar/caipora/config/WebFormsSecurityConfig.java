package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.util.UserRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.time.Duration;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableMethodSecurity()
public class WebFormsSecurityConfig {

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
            @Value("${webForms.rememberMeKey}") final String rememberMeKey) throws Exception {
        return http.cors(withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/actuator/**"))
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(EndpointRequest.to("health")).hasAnyAuthority(UserRoles.ROLE_ACTUATOR_ACCESS)
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasAnyAuthority(UserRoles.ROLE_SWAGGER_ACCESS)
                    .anyRequest().permitAll())
                .formLogin(form -> form
                    .loginPage("/login")
                    .usernameParameter("name")
                    .failureUrl("/login?loginError=true"))
                .rememberMe(rememberMe -> rememberMe
                    .tokenValiditySeconds(((int)Duration.ofDays(180).getSeconds()))
                    .rememberMeParameter("rememberMe")
                    .key(rememberMeKey))
                .logout(logout -> logout
                    .logoutSuccessUrl("/login?logoutSuccess=true")
                    .deleteCookies("SESSION"))
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login?loginRequired=true")))
                .build();
    }
}
