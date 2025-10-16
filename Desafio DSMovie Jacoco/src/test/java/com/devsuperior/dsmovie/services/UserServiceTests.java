package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtil userUtil;

    private String username;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        username = "maria@gmail.com";
        user = UserFactory.createUserEntity();
    }

    @Test
    public void authenticatedShouldReturnUserEntityWhenUserExists() {
        when(userUtil.getLoggedUsername()).thenReturn(username);
        when(repository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        UserEntity result = service.authenticated();

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userUtil).getLoggedUsername();
        verify(repository).findByUsername(username);
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        when(userUtil.getLoggedUsername()).thenReturn(username);
        when(repository.findByUsername(username)).thenReturn(java.util.Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.authenticated());

        verify(userUtil).getLoggedUsername();
        verify(repository).findByUsername(username);
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        List<UserDetailsProjection> projections = UserDetailsFactory.createCustomAdminClientUser(username);
        when(repository.searchUserAndRolesByUsername(username)).thenReturn(projections);

        UserDetails details = service.loadUserByUsername(username);

        assertNotNull(details);
        assertEquals(username, details.getUsername());
        var authorities = details.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_CLIENT"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
        verify(repository).searchUserAndRolesByUsername(username);
    }

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        when(repository.searchUserAndRolesByUsername(anyString())).thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));

        verify(repository).searchUserAndRolesByUsername(username);
    }
}
