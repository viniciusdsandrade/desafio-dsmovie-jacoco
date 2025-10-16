package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.devsuperior.dsmovie.services.impl.MovieServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.exceptions.DatabaseException;
import com.devsuperior.dsmovie.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.tests.MovieFactory;

@ExtendWith(MockitoExtension.class)
public class MovieServiceImplTests {

    @InjectMocks
    private MovieServiceImpl service;

    @Mock
    private MovieRepository repository;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;

    private MovieEntity movie;
    private PageRequest pageable;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        movie = MovieFactory.createMovieEntity();
        pageable = PageRequest.of(0, 12);
    }

    @Test
    public void findAllShouldReturnPagedMovieDTO() {
        List<MovieEntity> list = Collections.singletonList(movie);
        Page<MovieEntity> page = new PageImpl<>(list, pageable, list.size());
        when(repository.searchByTitle(anyString(), eq(pageable))).thenReturn(page);

        Page<MovieDTO> result = service.findAll("", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        // compatível com JDK 17
        assertEquals(movie.getTitle(), result.getContent().get(0).title());
        verify(repository, times(1)).searchByTitle(anyString(), eq(pageable));
    }

    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(movie));

        MovieDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(movie.getId(), result.id());
        assertEquals(movie.getTitle(), result.title());
        verify(repository, times(1)).findById(existingId);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
        verify(repository, times(1)).findById(nonExistingId);
    }

    @Test
    public void insertShouldReturnMovieDTO() {
        when(repository.save(any(MovieEntity.class))).thenAnswer(inv -> {
            MovieEntity e = inv.getArgument(0);
            e.setId(existingId);
            return e;
        });

        MovieDTO input = MovieFactory.createMovieDTO();
        MovieDTO result = service.insert(input);

        assertNotNull(result.id());
        assertEquals(input.title(), result.title());
        assertEquals(input.image(), result.image());
        assertEquals(input.score(), result.score());
        assertEquals(input.count(), result.count());
        verify(repository, times(1)).save(any(MovieEntity.class));
    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {
        when(repository.getReferenceById(existingId))
                .thenReturn(new MovieEntity(existingId, "Old", 1.0, 5, "img"));
        when(repository.save(any(MovieEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MovieDTO dto = new MovieDTO(
                existingId,
                "Updated Title",
                4.7,
                23,
                "https://example.com/updated.png"
        );

        MovieDTO result = service.update(existingId, dto);

        assertEquals(existingId, result.id());
        assertEquals("Updated Title", result.title());
        assertEquals(4.7, result.score());
        assertEquals(23, result.count());
        assertEquals("https://example.com/updated.png", result.image());
        verify(repository).getReferenceById(existingId);
        verify(repository).save(any(MovieEntity.class));
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.getReferenceById(nonExistingId)).thenThrow(new EntityNotFoundException());

        MovieDTO dto = MovieFactory.createMovieDTO();

        assertThrows(ResourceNotFoundException.class, () -> service.update(nonExistingId, dto));
        verify(repository, times(1)).getReferenceById(nonExistingId);
        verify(repository, never()).save(any());
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        when(repository.existsById(existingId)).thenReturn(true);
        doNothing().when(repository).deleteById(existingId);

        assertDoesNotThrow(() -> service.delete(existingId));

        verify(repository, times(1)).existsById(existingId);
        verify(repository, times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.existsById(nonExistingId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistingId));

        verify(repository, times(1)).existsById(nonExistingId);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        when(repository.existsById(dependentId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("FK violation")).when(repository).deleteById(dependentId);

        assertThrows(DatabaseException.class, () -> service.delete(dependentId));

        verify(repository, times(1)).existsById(dependentId);
        verify(repository, times(1)).deleteById(dependentId);
    }
}
