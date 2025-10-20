package com.devsuperior.dsmovie.dto;

import com.devsuperior.dsmovie.entities.ScoreEntity;
import jakarta.validation.constraints.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ScoreDTO {

    @NotNull(message = "movieId é obrigatório")
    private Long movieId;

    @NotNull(message = "score é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "score deve ser \u2265 0.0")
    @DecimalMax(value = "5.0", inclusive = true, message = "score deve ser \u2264 5.0")
    private Double score;

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}

