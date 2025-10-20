package com.devsuperior.dsmovie.services.impl;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.exceptions.DatabaseException;
import com.devsuperior.dsmovie.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.MovieService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository repository;

    public MovieServiceImpl(MovieRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDTO> findAll(String title, Pageable pageable) {
        Page<MovieEntity> result = repository.searchByTitle(title, pageable);
        return result.map(MovieDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieDTO findById(Long id) {
        MovieEntity result = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
        return new MovieDTO(result);
    }

    @Override
    @Transactional
    public MovieDTO insert(MovieDTO dto) {
        MovieEntity entity = new MovieEntity();
        copyDtoToEntity(dto, entity);
        repository.save(entity);
        return new MovieDTO(entity);
    }

    @Override
    @Transactional
    public MovieDTO update(Long id, MovieDTO dto) {
        try {
            MovieEntity entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            repository.save(entity);
            return new MovieDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("Recurso não encontrado");
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(MovieDTO dto, MovieEntity entity) {
        entity.setTitle(dto.title());
        entity.setScore(dto.score());
        entity.setCount(dto.count());
        entity.setImage(dto.image());
    }
}