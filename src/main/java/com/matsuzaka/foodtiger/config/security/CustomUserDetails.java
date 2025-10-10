package com.matsuzaka.foodtiger.config.security;

import com.matsuzaka.foodtiger.dao.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails build(User user) {
        // 將用戶的 Role 轉換為 Spring Security 的 GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
