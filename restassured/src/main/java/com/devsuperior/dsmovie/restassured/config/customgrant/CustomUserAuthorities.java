package com.devsuperior.dsmovie.restassured.config.customgrant;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record CustomUserAuthorities(
        String username,
        Collection<? extends GrantedAuthority> authorities
) {
    public CustomUserAuthorities {
        if (username == null) throw new NullPointerException("username");
        if (authorities == null) throw new NullPointerException("authorities");
        authorities = List.copyOf(authorities);
    }
}
