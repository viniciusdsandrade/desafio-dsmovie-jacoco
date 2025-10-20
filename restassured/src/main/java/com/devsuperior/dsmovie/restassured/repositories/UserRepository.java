package com.devsuperior.dsmovie.restassured.repositories;

import com.devsuperior.dsmovie.restassured.entities.UserEntity;
import com.devsuperior.dsmovie.restassured.projections.UserDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByUsername(String username);
	
	@Query(nativeQuery = true, value = """
			SELECT tb_user.username AS username, tb_user.password, tb_role.id AS roleId, tb_role.authority
			FROM tb_user
			INNER JOIN tb_user_role ON tb_user.id = tb_user_role.user_id
			INNER JOIN tb_role ON tb_role.id = tb_user_role.role_id
			WHERE tb_user.username = :username
		""")
	List<UserDetailsProjection> searchUserAndRolesByUsername(String username);
}
