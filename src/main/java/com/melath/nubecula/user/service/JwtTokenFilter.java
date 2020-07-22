package com.melath.nubecula.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    public static final String TOKEN = "nubecula_token";
    private final com.melath.nubecula.user.service.JwtTokenServices jwtTokenServices;

    // this is called for every request that comes in (unless its filtered out before in the chain)
    @Override
    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        Optional<Cookie> jwtToken =
                Arrays.stream(Optional.ofNullable(req.getCookies()).orElse(new Cookie[]{}))
                        .filter(cookie -> cookie.getName().equals(TOKEN))
                        .findFirst();

        if (jwtToken.isPresent()) {
            UsernamePasswordAuthenticationToken userToken = jwtTokenServices.validateTokenAndExtractUserSpringToken(jwtToken.get().getValue());
            SecurityContextHolder.getContext().setAuthentication(userToken);

        }

        // process the next filter.
        filterChain.doFilter(req, res);
    }
}
