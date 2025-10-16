package com.devsuperior.dsmovie.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.NONE;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
@Setter
@Getter
@Entity
@Table(name = "tb_movie")
public class MovieEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private String title;
	private Double score;
	private Integer count;
	private String image;
	
	@OneToMany(mappedBy = "id.movie")
    @Setter(NONE)
	private Set<ScoreEntity> scores = new HashSet<>();

	public MovieEntity(Long id, String title, Double score, Integer count, String image) {
		this.id = id;
		this.title = title;
		this.score = score;
		this.count = count;
		this.image = image;
	}
}
