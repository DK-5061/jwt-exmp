package com.example.jwtexmp.config;

import com.example.jwtexmp.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        Optional<String> jwt = Optional.ofNullable(httpServletRequest.getHeader("Authorization"))
                .map(s -> s.substring(7));
        Optional<String> username = jwt.map(jwtUtil::extractUsername);

        if (username.isPresent() && Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).isEmpty()) {
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username.orElseThrow());
            Optional.ofNullable(userDetails)
                    .filter(u -> jwtUtil.validateToken(jwt.orElseThrow(), u))
                    .ifPresent(u -> {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                u, null, u.getAuthorities()
                        );
                        usernamePasswordAuthenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)
                        );
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    });
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
