package com.jvj28.homeworks.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final ApiAuthControllerService userDetailsService;
    private final ApiRequestAuthorizationFilter authorizationFilter;

    public WebSecurityConfig(ApiAuthControllerService userDetailsService,
                             ApiRequestAuthorizationFilter authorizationFilter) {
        this.userDetailsService = userDetailsService;
        this.authorizationFilter = authorizationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                    .mvcMatchers(
                        "/api/help", "/metrics", "/error", "/authenticate",
                        "/index.html", "/public/**", "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/api-docs.yaml").permitAll()
                    .anyRequest().authenticated()
                .and()
                    .httpBasic().realmName(Realm.BASIC).authenticationEntryPoint(authBasicFailedEntryPoint())
                .and()
                    .exceptionHandling().authenticationEntryPoint(authJwtFailedEntryPoint())
                .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .formLogin().disable();

        // Add a filter to validate the tokens with every request
        http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationEntryPoint authBasicFailedEntryPoint() {
        return new ApiAuthBasicEntryPoint();
    }

    @Bean
    public AuthenticationEntryPoint authJwtFailedEntryPoint() {
        return new ApiAuthJwtEntryPoint();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
    }

}
