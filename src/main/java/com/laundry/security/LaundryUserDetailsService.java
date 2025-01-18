package com.laundry.security;

import com.laundry.entity.User;
import com.laundry.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaundryUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public LaundryUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String roleName = user.getRole();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);

        return new LaundryUserDetails(
                user,
                java.util.List.of(authority)
        );
    }
}
