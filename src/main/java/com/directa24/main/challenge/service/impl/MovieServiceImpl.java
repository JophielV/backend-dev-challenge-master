package com.directa24.main.challenge.service.impl;

import com.directa24.main.challenge.data.ApiResponse;
import com.directa24.main.challenge.data.Movie;
import com.directa24.main.challenge.exception.RequestException;
import com.directa24.main.challenge.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieServiceImpl implements MovieService {

    private static final String PAGE = "page";
    private final String apiUrl;
    private final WebClient webClient;

    public MovieServiceImpl(@Value("${apiService.url}") final String apiUrl,
                            WebClient webClient) {
        this.apiUrl = apiUrl;
        this.webClient = webClient;
    }

    @Override
    public List<String> getDirectors(int threshold) {
        List<Movie> allMovies = new ArrayList<>();

        // get the initial response of data for page 1, this will also return total_pages field
        // which will determine how many times we will be iterating to get all movies at the end of the page
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam(PAGE, String.valueOf(1));
        ApiResponse initialApiResponse = webClient
                .get()
                .uri(builder.buildAndExpand(apiUrl).toUri())
                .retrieve()
                .bodyToMono(ApiResponse.class).block();

        if (initialApiResponse == null) return List.of();

        allMovies.addAll(initialApiResponse.getData());
        allMovies.addAll(getMoviesForSucceedingPages(initialApiResponse));

        return allMovies.stream()
                .collect(Collectors.groupingBy(Movie::getDirector, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Movie> getMoviesForSucceedingPages(ApiResponse initialApiResponse) {
        final List<URI> urls = new ArrayList<>();
        // composing request urls starting now at 2nd page up to the end of the page
        int i = 2;
        while (i <= initialApiResponse.getTotalPages()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam(PAGE, String.valueOf(i));
            urls.add(builder.buildAndExpand(apiUrl).toUri());
            i++;
        }

        if (!CollectionUtils.isEmpty(urls)) {
            // send requests asynchronously as each succeeding request do not depend on each other
            final Flux<ApiResponse> events = Flux.fromIterable(urls)
                    .flatMap(uri -> request(webClient, uri));
            return Objects.requireNonNull(events
                            .collect(Collectors.toList())
                            .block())
                    .stream()
                    .map(ApiResponse::getData)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private static Mono<ApiResponse> request(WebClient webClient, URI uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(ApiResponse.class)
                .map(HttpEntity::getBody)
                .onErrorResume(RequestException.class, err -> Mono.just(new ApiResponse()));
    }

}
