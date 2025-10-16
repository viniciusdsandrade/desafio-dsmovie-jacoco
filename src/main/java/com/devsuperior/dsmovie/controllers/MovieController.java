package com.devsuperior.dsmovie.controllers;

import java.net.URI;

import com.devsuperior.dsmovie.services.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.services.impl.MovieServiceImpl;

import jakarta.validation.Valid;
import org.springframework.web.util.HtmlUtils;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;


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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> insert(@Valid @RequestBody MovieDTO dto) {
        MovieDTO saved = movieService.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();
        return created(uri).body(sanitize(saved));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value="/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> update(@PathVariable Long id, @Valid @RequestBody MovieDTO dto) {
        MovieDTO saved = movieService.update(id, dto);
        return ok().body(sanitize(saved));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
