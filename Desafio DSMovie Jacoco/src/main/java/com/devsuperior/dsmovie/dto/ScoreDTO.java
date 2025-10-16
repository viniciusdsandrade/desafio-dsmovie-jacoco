package com.devsuperior.dsmovie.dto;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.devsuperior.dsmovie.entities.ScoreEntity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ScoreDTO(

        @NotNull(message = "Required field")
        Long movieId,

        @PositiveOrZero(message = "Score should be greater than or equal to zero")
        @Max(value = 5, message = "Score should not be greater than five")
        Double score
) {
    private static final DecimalFormat df =
            new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public ScoreDTO {
        if (score != null) {
            score = Double.valueOf(df.format(score));
        }
    }

    public ScoreDTO(ScoreEntity entity) {
        this(
                entity.getId().getMovie().getId(),
                entity.getValue() == null ? null : Double.valueOf(df.format(entity.getValue()))
        );
    }
}
