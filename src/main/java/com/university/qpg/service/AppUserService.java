package com.university.qpg.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.university.qpg.model.AppUser;
import com.university.qpg.repository.AppUserRepository;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser registerUser(String username, String email, String rawPassword) {
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setEmail(email);
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        appUser.setRole("ROLE_USER");
        return appUserRepository.save(appUser);
    }

    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
                appUser.getUsername(),
                appUser.getPassword(),
                java.util.List.of(new SimpleGrantedAuthority(appUser.getRole())));
    }
}
