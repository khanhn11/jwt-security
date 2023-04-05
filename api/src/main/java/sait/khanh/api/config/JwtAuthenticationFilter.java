package sait.khanh.api.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JWT Validation Process
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetialsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // get the authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        /**
         * check if the authorization is null or does not come from the Bearer token.
         * Then execute the filter chain to pass the next request or response to next
         * filter
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // get the jwt token from the authorization header (bearer)
        jwt = authHeader.substring(7);
        // extract the userEmail from JWT token;
        userEmail = jwtService.extractUsername(jwt);

        // check if the user is authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // check if the user is existed in the database
            UserDetails userDetails = this.userDetialsService.loadUserByUsername(userEmail);

            // check if the toke is valid
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // if the user is valid update the authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response); // pass to the next filter
    }

}
