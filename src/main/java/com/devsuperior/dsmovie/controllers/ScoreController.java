package com.devsuperior.dsmovie.controllers;

import com.devsuperior.dsmovie.services.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.services.impl.ScoreServiceImpl;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/scores", produces = APPLICATION_JSON_VALUE)
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreServiceImpl scoreService) {
        this.scoreService = scoreService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> saveScore(@Valid @RequestBody ScoreDTO dto) {
        MovieDTO movieDTO = scoreService.saveScore(dto);
        return ok(movieDTO);
    }
}
