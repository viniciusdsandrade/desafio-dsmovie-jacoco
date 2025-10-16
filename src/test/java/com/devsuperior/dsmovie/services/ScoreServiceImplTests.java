package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.impl.ScoreServiceImpl;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScoreServiceImplTests {

    @InjectMocks
    private ScoreServiceImpl service;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private ScoreDTO scoreDTO;
    private MovieEntity movie;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        scoreDTO = ScoreFactory.createScoreDTO();
        movie = new MovieEntity(1L, "Test Movie", 0.0, 0, "img");
        user = UserFactory.createUserEntity();
    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {
        when(userService.authenticated()).thenReturn(user);
        when(movieRepository.findById(scoreDTO.movieId())).thenReturn(Optional.of(movie));
        when(scoreRepository.saveAndFlush(any(ScoreEntity.class))).thenAnswer(inv -> {
            ScoreEntity s = inv.getArgument(0);
            s.getId().getMovie().getScores().add(s);
            return s;
        });
        when(movieRepository.save(any(MovieEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieDTO result = service.saveScore(scoreDTO);

        assertNotNull(result);
        assertEquals(movie.getId(), result.id());
        assertEquals(1, result.count());
        assertEquals(ScoreFactory.scoreValue, result.score());
        verify(userService).authenticated();
        verify(movieRepository).findById(scoreDTO.movieId());
        verify(scoreRepository).saveAndFlush(any(ScoreEntity.class));
        verify(movieRepository).save(any(MovieEntity.class));
    }

    @Test
    public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
        when(userService.authenticated()).thenReturn(user);
        when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.saveScore(scoreDTO));

        verify(userService).authenticated();
        verify(movieRepository).findById(scoreDTO.movieId());
        verify(scoreRepository, never()).saveAndFlush(any());
        verify(movieRepository, never()).save(any());
    }
}
