package com.devsuperior.dsmovie.restassured.repositories;

import com.devsuperior.dsmovie.restassured.entities.ScoreEntity;
import com.devsuperior.dsmovie.restassured.entities.ScoreEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends JpaRepository<ScoreEntity, ScoreEntityPK> {

}