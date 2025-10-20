package com.devsuperior.dsmovie.entities;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ScoreEntity that = (ScoreEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
