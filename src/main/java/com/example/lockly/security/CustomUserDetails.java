package com.example.lockly.security;

import com.example.lockly.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

<<<<<<< HEAD
    // This method returns the authorities (roles) of the user.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
=======
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
<<<<<<< HEAD
=======
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
