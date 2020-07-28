package com.melath.nubecula.user.config;

import com.melath.nubecula.user.service.JwtTokenFilter;
import com.melath.nubecula.user.service.JwtTokenServices;
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
                .antMatchers("/directories/").authenticated() // allowed only when signed in
                .antMatchers("/directories/{id}").authenticated() // allowed only when signed in
                .antMatchers("/directories/directory").authenticated() // allowed only when signed in
                .antMatchers("/directories/directory/{id}").authenticated() // allowed only when signed in
                .antMatchers("/files").authenticated() // allowed only when signed in
                .antMatchers("/files/").authenticated() // allowed only when signed in
                .antMatchers("/files/{id}").authenticated() // allowed only when signed in
                .antMatchers("/trash-bin").authenticated() // allowed only when signed in
                .antMatchers("/trash-bin").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/toggle-share/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/directories/toggle-share/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/files/toggle-share/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/replace").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/directories/replace").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/files/replace").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/copy").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/directories/copy").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/files/copy").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.GET, "/size/{id}").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.GET, "/search").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.GET, "/users/current").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/users/rename").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.PUT, "/users/description").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.POST, "/users/delete").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.POST, "/users/image").authenticated() // allowed only when signed in
                .antMatchers(HttpMethod.GET, "/users/{username}").permitAll()
                .antMatchers(HttpMethod.GET, "/users").permitAll()
                .antMatchers(HttpMethod.GET, "/users/").permitAll()
                .antMatchers(HttpMethod.GET, "/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/public").permitAll()
                .antMatchers(HttpMethod.GET, "/public/{username}").permitAll()
                .antMatchers(HttpMethod.GET, "/public/{username}/directories/directory/{id}").permitAll()
                .antMatchers(HttpMethod.GET, "/public/{username}/files/{id}").permitAll()
                .antMatchers(HttpMethod.GET, "/public/search").permitAll()
                .anyRequest().denyAll() // anything else is denied
                // NEW PART:
                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenServices), UsernamePasswordAuthenticationFilter.class)
                .cors();
    }

}
