package pe.chambitalabs.platform.iam.infrastructure.authorization;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.TokenService;

import java.io.IOException;
import java.util.List;

/** Reads the Bearer JWT of each request and authenticates the user with its role. */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final TokenService tokenService;

    public BearerAuthorizationRequestFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIX) && !request.getRequestURI().startsWith("/internal/")) {
            tokenService.validateToken(header.substring(PREFIX.length())).ifPresent(user -> {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role()));
                var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        chain.doFilter(request, response);
    }
}
