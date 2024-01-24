package com.polimi.PPP.CodeKataBattle.Filters;

import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidRoleException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidTokenException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUserIdException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUsernameException;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnunm;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Security.UserIdAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;

    public JWTAuthenticationFilter(JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token == null || !jwtHelper.validateToken(token)) {
            //Invalid token, respond with 401 Unauthorized
            throw new InvalidTokenException("Invalid token provided");
        }

        Long userId = jwtHelper.extractUserId(token);

        if (userId == null) {
            throw new InvalidUsernameException("Invalid userId provided in the token");
        }

        Authentication auth = getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Authentication getAuthentication(String token) {
        // Extract information from token (e.g., username, roles)
        Long userId = jwtHelper.extractUserId(token);
        return new UserIdAuthenticationToken(userId);
    }


}
