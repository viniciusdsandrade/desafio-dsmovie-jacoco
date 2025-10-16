package com.devsuperior.dsmovie.entities;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "tb_score")
public class ScoreEntity {

	@EmbeddedId
	private ScoreEntityPK id = new ScoreEntityPK();
	
	@Column(name = "score_value")
	private Double value;

	public void setMovie(MovieEntity movie) {
		id.setMovie(movie);
	}

	public void setUser(UserEntity user) {
		id.setUser(user);
	}
	
	public ScoreEntityPK getId() {
		return id;
	}

	public void setId(ScoreEntityPK id) {
		this.id = id;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
