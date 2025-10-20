package com.devsuperior.dsmovie.services.impl;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.ScoreService;
import com.devsuperior.dsmovie.services.UserService;
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
                .orElseThrow(() -> new ResourceNotFoundException("Id não encontrado " + dto.movieId()));

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
