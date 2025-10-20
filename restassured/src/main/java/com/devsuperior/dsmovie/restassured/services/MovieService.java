package com.devsuperior.dsmovie.restassured.services;

import com.devsuperior.dsmovie.restassured.dto.MovieDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface MovieService {
    @Transactional(readOnly = true)
    Page<MovieDTO> findAll(String title, Pageable pageable);

    @Transactional(readOnly = true)
    MovieDTO findById(Long id);

    @Transactional
    MovieDTO insert(MovieDTO dto);

    @Transactional
    MovieDTO update(Long id, MovieDTO dto);

    @Transactional
    void delete(Long id);
}
