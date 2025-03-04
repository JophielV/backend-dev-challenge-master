package com.directa24.main.challenge.service.impl;

import com.directa24.main.challenge.data.ApiResponse;
import com.directa24.main.challenge.data.Movie;
import com.directa24.main.challenge.service.MovieService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Service
public class MovieServiceImpl implements MovieService {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public MovieServiceImpl(RestTemplate restTemplate,
                            @Value("${apiService.url}") final String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    public List<String> getDirectors(int threshold) {
        List<Movie> allMovies = new ArrayList<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("page", String.valueOf(1));

        ApiResponse initialApiResponse = WebClient.create()
                .get()
                .uri(builder.buildAndExpand(apiUrl).toUri())
                .retrieve()
                .bodyToMono(ApiResponse.class).block();
        allMovies.addAll(initialApiResponse.getData());

        final List<String> urls = new ArrayList<>();

        int i = 2;
        while (i <= initialApiResponse.getTotalPages()) {
            builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("page", String.valueOf(i));
            urls.add(builder.buildAndExpand(apiUrl).toUri().toString());
            i++;
        }

        final Flux<ApiResponse> events = Flux.fromIterable(urls)
                .flatMap(url -> request(url));

        List<Movie> succeedingMovies = events
                .collect(Collectors.toList())
                .block()
                .stream()
                .map(a -> a.getData())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        allMovies.addAll(succeedingMovies);

       /*Mono<List<ApiResponse>> x = events.collect(Collectors.toList());
       List<ApiResponse> y = x.block();
       y.forEach(a -> {
           allMovies.addAll(a.getData());
       });*/

        //sendEmail(events);



        return allMovies.stream().collect(Collectors.groupingBy(Movie::getDirector, Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > threshold).
                map(Map.Entry::getKey).sorted()
                .collect(Collectors.toList());
    }

    private static void sendEmail(Flux<ApiResponse> report) {
        final String formattedReport = report
                .map(details -> String.format("Error on %s. status: %d. Reason: %s"))
                // collecting (or reducing, folding, etc.) allows to gather all upstream results to use them as a single value downstream.
                .collect(Collectors.joining(System.lineSeparator(), "REPORT:"+System.lineSeparator(), ""))
                // In a real-world scenario, replace this with a subscribe or chaining to another reactive operation.
                .block();
        log.info(formattedReport);
    }

    private static void log(ExchangeDetails details) {
        if (details.status >= 0 && HttpStatus.valueOf(details.status).is2xxSuccessful()) {
            log.info("Success on: "+details.url);
        } else {
            log.warn(
                    "Status {0} on {1}. Reason: {2}",
                    new Object[]{
                            details.status,
                            details.url,
                            details.error == null ? "None" : details.error.getMessage()
                    });
        }
    }

    private static Mono<ApiResponse> request(String url) {
        System.out.println("---------- url: " + url + ", " + new Date());
        return WebClient.create(url).get()
                .retrieve()
                // workaround to counter fail-fast behavior: create a special error that will be converted back to a result
                .toEntity(ApiResponse.class)
                .map(HttpEntity::getBody)
                // Convert back custom error to result
                .onErrorResume(RequestException.class, err -> Mono.just(new ApiResponse()));
    }

    public static class ExchangeDetails {
        final String url;
        final int status;
        final Exception error;

        public ExchangeDetails(String url, int status, Exception error) {
            this.url = url;
            this.status = status;
            this.error = error;
        }
    }

    private static class RequestException extends RuntimeException {
        final HttpStatus status;
        final Exception cause;

        public RequestException(HttpStatus status, Exception cause) {
            this.status = status;
            this.cause = cause;
        }
    }

}
