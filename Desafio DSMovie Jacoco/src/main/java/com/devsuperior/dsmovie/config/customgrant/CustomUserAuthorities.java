package com.devsuperior.dsmovie.config.customgrant;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

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
