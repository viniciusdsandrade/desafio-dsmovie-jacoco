package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import org.springframework.transaction.annotation.Transactional;

public interface ScoreService {
    @Transactional
    MovieDTO saveScore(ScoreDTO dto);
}
