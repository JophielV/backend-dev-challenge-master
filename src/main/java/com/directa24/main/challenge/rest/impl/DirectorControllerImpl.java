package com.directa24.main.challenge.rest.impl;

import com.directa24.main.challenge.rest.DirectorController;
import com.directa24.main.challenge.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/directors")
public class DirectorControllerImpl implements DirectorController {

    private final MovieService movieService;

    public DirectorControllerImpl(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<String>> getDirectors(@RequestParam int threshold) {
        return ResponseEntity.ok(movieService.getDirectors(threshold));
    }

}
