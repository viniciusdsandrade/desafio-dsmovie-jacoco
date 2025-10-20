package com.devsuperior.dsmovie.restassured.services;

import com.devsuperior.dsmovie.restassured.dto.MovieDTO;
import com.devsuperior.dsmovie.restassured.dto.ScoreDTO;
import org.springframework.transaction.annotation.Transactional;

public interface ScoreService {
    @Transactional
    MovieDTO saveScore(ScoreDTO dto);
}
