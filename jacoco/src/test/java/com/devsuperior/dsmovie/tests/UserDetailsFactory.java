package com.devsuperior.dsmovie.tests;

import com.devsuperior.dsmovie.projections.UserDetailsProjection;

import java.util.ArrayList;
import java.util.List;

/** ÚNICA versão desta fábrica. Não duplique este arquivo. */
public class UserDetailsFactory {

    public static List<UserDetailsProjection> createCustomClientUser(String username) {
        List<UserDetailsProjection> list = new ArrayList<>();
        list.add(new UserDetailsImpl(username, "123", 1L, "ROLE_CLIENT"));
        return list;
    }

    public static List<UserDetailsProjection> createCustomAdminUser(String username) {
        List<UserDetailsProjection> list = new ArrayList<>();
        list.add(new UserDetailsImpl(username, "123", 2L, "ROLE_ADMIN"));
        return list;
    }

    public static List<UserDetailsProjection> createCustomAdminClientUser(String username) {
        List<UserDetailsProjection> list = new ArrayList<>();
        list.add(new UserDetailsImpl(username, "123", 1L, "ROLE_CLIENT"));
        list.add(new UserDetailsImpl(username, "123", 2L, "ROLE_ADMIN"));
        return list;
    }

    private static final class UserDetailsImpl implements UserDetailsProjection {
        private final String username;
        private final String password;
        private final Long roleId;
        private final String authority;

        private UserDetailsImpl(String username, String password, Long roleId, String authority) {
            this.username = username;
            this.password = password;
            this.roleId = roleId;
            this.authority = authority;
        }

        @Override public String getUsername() { return username; }
        @Override public String getPassword() { return password; }
        @Override public Long getRoleId() { return roleId; }
        @Override public String getAuthority() { return authority; }
    }
}
