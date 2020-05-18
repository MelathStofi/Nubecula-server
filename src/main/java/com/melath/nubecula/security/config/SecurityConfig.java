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

    @Autowired
    private JwtTokenServices jwtTokenServices;

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
                .antMatchers("/**").permitAll()
                .antMatchers("/auth/**").permitAll() // allowed by anyone
                .antMatchers(HttpMethod.GET, "/quiz-service/categories").authenticated() // allowed only when signed in
                .antMatchers("/quiz-service/customquizzes/**").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.GET, "/quiz-service/questions/**").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.POST, "/quiz-service/questions/add").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/quiz-service/questions/**").hasAuthority("ROLE_ADMIN") // allowed only when signed in
                .antMatchers(HttpMethod.DELETE, "/quiz-service/questions/**").hasAuthority("ROLE_ADMIN") // allowed only when signed in
                .antMatchers("/quiz-service/types/**").authenticated() // allowed only when signed in
                .anyRequest().denyAll() // anything else is denied
                // NEW PART:
                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenServices), UsernamePasswordAuthenticationFilter.class)
                .cors();
    }

}
