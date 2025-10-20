package com.devsuperior.dsmovie.controllers;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.services.ScoreService;
import com.devsuperior.dsmovie.services.impl.ScoreServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "/scores", produces = APPLICATION_JSON_VALUE)
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreServiceImpl scoreService) {
        this.scoreService = scoreService;
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> saveScore(@RequestBody ScoreDTO dto) {
        MovieDTO movieDTO = scoreService.saveScore(dto);
        return ok(movieDTO);
    }
}
