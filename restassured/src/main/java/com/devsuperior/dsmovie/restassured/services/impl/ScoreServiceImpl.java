package com.devsuperior.dsmovie.restassured.services.impl;

import com.devsuperior.dsmovie.restassured.dto.MovieDTO;
import com.devsuperior.dsmovie.restassured.dto.ScoreDTO;
import com.devsuperior.dsmovie.restassured.entities.MovieEntity;
import com.devsuperior.dsmovie.restassured.entities.ScoreEntity;
import com.devsuperior.dsmovie.restassured.entities.UserEntity;
import com.devsuperior.dsmovie.restassured.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.restassured.repositories.MovieRepository;
import com.devsuperior.dsmovie.restassured.repositories.ScoreRepository;
import com.devsuperior.dsmovie.restassured.services.ScoreService;
import com.devsuperior.dsmovie.restassured.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreServiceImpl implements ScoreService {

    private final UserService userService;
    private final MovieRepository movieRepository;
    private final ScoreRepository scoreRepository;

    public ScoreServiceImpl(
            UserService userService,
            MovieRepository movieRepository,
            ScoreRepository scoreRepository
    ) {
        this.userService = userService;
        this.movieRepository = movieRepository;
        this.scoreRepository = scoreRepository;
    }

    @Override
    @Transactional
    public MovieDTO saveScore(ScoreDTO dto) {
        UserEntity user = userService.authenticated();

        MovieEntity movie = movieRepository.findById(dto.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));

        ScoreEntity score = new ScoreEntity();
        score.setMovie(movie);
        score.setUser(user);
        score.setValue(dto.score());

        scoreRepository.saveAndFlush(score);

        double sum = movie.getScores()
                .stream()
                .mapToDouble(ScoreEntity::getValue)
                .sum();

        double avg = sum / movie.getScores().size();

        movie.setScore(avg);
        movie.setCount(movie.getScores().size());

        movieRepository.save(movie);

        return new MovieDTO(movie);
    }
}
