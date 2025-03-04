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

    private final String apiUrl;
    private final WebClient webClient;

    public MovieServiceImpl(@Value("${apiService.url}") final String apiUrl,
                            WebClient webClient) {
        this.apiUrl = apiUrl;
        this.webClient = webClient;
    }

    public List<String> getDirectors(int threshold) {
        List<Movie> allMovies = new ArrayList<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("page", String.valueOf(1));

        ApiResponse initialApiResponse = webClient
                .get()
                .uri(builder.buildAndExpand(apiUrl).toUri())
                .retrieve()
                .bodyToMono(ApiResponse.class).block();

        if (initialApiResponse == null) return new ArrayList<>();

        allMovies.addAll(initialApiResponse.getData());
        allMovies.addAll(callSucceedingRequests(initialApiResponse));

        return allMovies.stream().collect(Collectors.groupingBy(Movie::getDirector, Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > threshold).
                map(Map.Entry::getKey).sorted()
                .collect(Collectors.toList());
    }

    private List<Movie> callSucceedingRequests(ApiResponse initialApiResponse) {
        final List<URI> urls = new ArrayList<>();
        int i = 2;
        while (i <= initialApiResponse.getTotalPages()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("page", String.valueOf(i));
            urls.add(builder.buildAndExpand(apiUrl).toUri());
            i++;
        }

        if (!CollectionUtils.isEmpty(urls)) {
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

        return new ArrayList<>();
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
