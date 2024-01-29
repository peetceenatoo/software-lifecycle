package com.polimi.PPP.CodeKataBattle.Filters;

import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidTokenException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUserIdException;
import com.polimi.PPP.CodeKataBattle.Model.JWTTokenUseCase;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Security.SubmissionAuthenticationToken;
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

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;

    public JWTAuthenticationFilter(JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip filter for public endpoints
        if (request.getMethod().equals("POST") && ("/api/users/signup".equals(requestURI) || "/api/users/login".equals(requestURI))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extracting and validating the token from the header
        String token = null;
        try{
            token = resolveToken(request);
            if (token == null || !jwtHelper.validateToken(token)) {
                //Invalid token, respond with 401 Unauthorized
                throw new InvalidTokenException("Invalid token provided");
            }
        }catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
        }

        // Extracting the useCase from the token
        String useCaseString = jwtHelper.extractUseCase(token);
        if (useCaseString == null ){
            throw new InvalidTokenException("Invalid useCase provided in the token");
        }
        JWTTokenUseCase useCase = null;
        try{
            useCase = JWTTokenUseCase.valueOf(useCaseString);
        }catch(Exception e) {
            throw new InvalidTokenException("Invalid useCase provided in the token");
        }

        // Checking if the token is valid for the request
        if(useCase == JWTTokenUseCase.SUBMISSION){
            if (!request.getMethod().equals("POST") || !requestURI.startsWith("api/battles/") || !requestURI.endsWith("/commit")){
                throw new InvalidTokenException("Invalid token for the request");
            }
        }
        if(useCase == JWTTokenUseCase.USER){
            if (requestURI.startsWith("api/battles/") && requestURI.endsWith("/commit")){
                throw new InvalidTokenException("Invalid token for the request");
            }
        }


        // Generating the context for the requests
        try{

            Long userId = jwtHelper.extractUserId(token);

            if (userId == null) {
                throw new InvalidUserIdException("Invalid userId provided in the token");
            }

            Authentication auth;

            if (useCase == JWTTokenUseCase.USER) {
                auth = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else if (useCase == JWTTokenUseCase.SUBMISSION) {
                try{
                    auth = getSubmissionAuthentication(token);
                }catch (Exception e) {
                    throw new InvalidTokenException("Invalid token for the request");
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
        }

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


    private Authentication getSubmissionAuthentication(String token) {
        // Extract information from token (e.g., username, roles)
        Long userId = jwtHelper.extractUserId(token);
        Long battleId = jwtHelper.extractBattleId(token);
        return new SubmissionAuthenticationToken(userId, battleId, "TEST");
    }




}
