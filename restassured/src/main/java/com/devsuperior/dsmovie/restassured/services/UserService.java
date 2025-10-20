package com.devsuperior.dsmovie.restassured.services;

import com.devsuperior.dsmovie.restassured.entities.RoleEntity;
import com.devsuperior.dsmovie.restassured.entities.UserEntity;
import com.devsuperior.dsmovie.restassured.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.restassured.repositories.UserRepository;
import com.devsuperior.dsmovie.restassured.utils.CustomUserUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository repository;
	private final CustomUserUtil userUtil;

    public UserService(UserRepository repository, CustomUserUtil userUtil) {
        this.repository = repository;
        this.userUtil = userUtil;
    }

	public UserEntity authenticated() {
		try {
			String username = userUtil.getLoggedUsername();
			return repository.findByUsername(username).get();
		}
		catch (Exception e) {
			throw new UsernameNotFoundException("Invalid user");
		}
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		List<UserDetailsProjection> result = repository.searchUserAndRolesByUsername(username);
		if (result.isEmpty()) {
			throw new UsernameNotFoundException("Email not found");
		}
		
		UserEntity user = new UserEntity();
		user.setUsername(result.getFirst().getUsername());
		user.setPassword(result.getFirst().getPassword());
		for (UserDetailsProjection projection : result) {
			user.addRole(new RoleEntity(projection.getRoleId(), projection.getAuthority()));
		}
		
		return user;
	}
}
