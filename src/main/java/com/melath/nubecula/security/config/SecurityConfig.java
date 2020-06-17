package com.melath.nubecula.security.config;

import com.melath.nubecula.security.service.JwtTokenFilter;
import com.melath.nubecula.security.service.JwtTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    private final JwtTokenServices jwtTokenServices;

    @Autowired
    public SecurityConfig(JwtTokenServices jwtTokenServices) {
        this.jwtTokenServices = jwtTokenServices;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/auth/**").permitAll() // allowed by anyone
                .antMatchers("/").authenticated() // allowed only when signed in
                .antMatchers("/{id}").authenticated() // allowed only when signed in
                .antMatchers("/directories").authenticated() // allowed only when signed in
                .antMatchers("/directories/{id}").authenticated() // allowed only when signed in
                .antMatchers("/files").authenticated() // allowed only when signed in
                .antMatchers("/files/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/toggle-share/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/directories/toggle-share/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/files/toggle-share/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.GET, "/users/**").permitAll()
                .anyRequest().denyAll() // anything else is denied
                // NEW PART:
                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenServices), UsernamePasswordAuthenticationFilter.class)
                .cors();
    }

}
