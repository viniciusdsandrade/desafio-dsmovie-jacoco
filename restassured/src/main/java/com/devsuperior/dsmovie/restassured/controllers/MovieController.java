package com.devsuperior.dsmovie.restassured.controllers;

import com.devsuperior.dsmovie.restassured.dto.MovieDTO;
import com.devsuperior.dsmovie.restassured.services.MovieService;
import com.devsuperior.dsmovie.restassured.services.impl.MovieServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;


@RestController
@RequestMapping(value = "/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieServiceImpl service) {
        this.movieService = service;
    }

    @GetMapping
    public Page<MovieDTO> findAll(
            @RequestParam(value = "title", defaultValue = "") String title,
            Pageable pageable
    ) {
        return movieService.findAll(title, pageable);
    }

    @GetMapping(value = "/{id}")
    public MovieDTO findById(@PathVariable Long id) {
        return movieService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> insert(@Valid @RequestBody MovieDTO dto) {
        MovieDTO saved = movieService.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();
        return created(uri).body(sanitize(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value="/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> update(@PathVariable Long id, @Valid @RequestBody MovieDTO dto) {
        MovieDTO saved = movieService.update(id, dto);
        return ok().body(sanitize(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<MovieDTO> delete(@PathVariable Long id) {
        movieService.delete(id);
        return noContent().build();
    }

    private static MovieDTO sanitize(MovieDTO d) {
        return new MovieDTO(
                d.id(),
                esc(d.title()),
                d.score(),
                d.count(),
                esc(d.image())
        );
    }

    private static String esc(String s) {
        return (s == null) ? null : HtmlUtils.htmlEscape(s);
    }
}
