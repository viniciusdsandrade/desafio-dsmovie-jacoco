package com.devsuperior.dsmovie.dto;

import jakarta.validation.constraints.*;

public record ScoreDTO(
        @NotNull(message = "campo obrigatório")
        Long movieId,

        @NotNull(message = "campo obrigatório")
        @DecimalMin(value = "0.0", message = "deve ser >= 0.0")
        @DecimalMax(value = "5.0", message = "deve ser <= 5.0")
        Double score
) {
}

