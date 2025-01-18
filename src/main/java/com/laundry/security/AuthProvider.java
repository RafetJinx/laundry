package com.laundry.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AuthProvider implements AuthenticationProvider {

    private final LaundryUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.username:}")
    private String propertyUsername;

    @Value("${app.auth.password:}")
    private String propertyPassword;

    public AuthProvider(LaundryUserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        if (!propertyUsername.isBlank()
                && !propertyPassword.isBlank()
                && username.equals(propertyUsername)
                && passwordEncoder.matches(rawPassword, propertyPassword)) {
            return new UsernamePasswordAuthenticationToken(
                    username,
                    rawPassword,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        try {
            LaundryUserDetails userDetails = (LaundryUserDetails) userDetailsService.loadUserByUsername(username);
            if (userDetails != null && passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        rawPassword,
                        userDetails.getAuthorities()
                );
            }
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Bad credentials");
        }

        throw new BadCredentialsException("Bad credentials");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
