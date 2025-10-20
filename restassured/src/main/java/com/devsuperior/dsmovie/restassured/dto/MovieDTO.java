package com.devsuperior.dsmovie.restassured.dto;

import com.devsuperior.dsmovie.restassured.entities.MovieEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record MovieDTO(
        Long id,

        @NotBlank(message = "Required field")
        @Size(min = 5, max = 80, message = "Title must be between 5 and 80 characters")
        String title,

        @PositiveOrZero(message = "Score should be greater than or equal to zero")
        Double score,

        @PositiveOrZero(message = "Count should be greater than or equal to zero")
        Integer count,

        @NotBlank(message = "Required field")
        @URL(message = "Field must be a valid url")
        String image
) {
    private static final DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public MovieDTO {
        if (score != null) {
            score = Double.valueOf(df.format(score));
        }
    }

    public MovieDTO(MovieEntity movie) {
        this(movie.getId(), movie.getTitle(), movie.getScore(), movie.getCount(), movie.getImage());
    }

    @Override
    public String toString() {
        return "MovieDTO [id=" + id + ", title=" + title + ", score=" + score + ", count=" + count + ", image=" + image + "]";
    }
}
