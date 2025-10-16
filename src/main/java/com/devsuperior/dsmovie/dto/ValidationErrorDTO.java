package com.devsuperior.dsmovie.dto;

import java.time.Instant;
import java.util.List;

public record ValidationErrorDTO(
        Instant timestamp,
        Integer status,
        String error,
        String path,
        List<FieldMessageDTO> errors
) implements ErrorDTO {
}