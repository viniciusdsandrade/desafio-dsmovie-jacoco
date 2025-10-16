package com.devsuperior.dsmovie.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;

@Service
public class ScoreService {

    private final UserService userService;
    private final MovieRepository movieRepository;
    private final ScoreRepository scoreRepository;

    public ScoreService(
            UserService userService,
            MovieRepository movieRepository,
            ScoreRepository scoreRepository
    ) {
        this.userService = userService;
        this.movieRepository = movieRepository;
        this.scoreRepository = scoreRepository;
    }

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
