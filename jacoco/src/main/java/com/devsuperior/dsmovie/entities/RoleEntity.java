package com.devsuperior.dsmovie.entities;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static jakarta.persistence.GenerationType.IDENTITY;
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "tb_role")
public class RoleEntity implements GrantedAuthority {

	@Setter
    @Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String authority;

    @Override
	public String getAuthority() {
		return authority;
	}}
