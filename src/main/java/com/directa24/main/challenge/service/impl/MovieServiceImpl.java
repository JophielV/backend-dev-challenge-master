package com.directa24.main.challenge.service.impl;

import com.directa24.main.challenge.data.ApiResponse;
import com.directa24.main.challenge.data.Movie;
import com.directa24.main.challenge.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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

    @PostConstruct
    public void init() {
        this.getDirectors(4);
    }

    public List<String> getDirectors(int threshold) {
        List<Movie> allMovies = new ArrayList<>();


        String finalUrl = apiUrl;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(finalUrl)
                // Add query parameter
                .queryParam("page", "1");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity entity = new HttpEntity(headers);

        ApiResponse apiResponse = restTemplate.exchange(builder.buildAndExpand(finalUrl).toUri(), GET, entity, ApiResponse.class).getBody();
        allMovies.addAll(apiResponse.getData());

        int i = 2;
        while (i <= apiResponse.getTotalPages()) {
            finalUrl = apiUrl;
            builder = UriComponentsBuilder.fromUriString(finalUrl)
                    // Add query parameter
                    .queryParam("page", String.valueOf(i));

            headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            entity = new HttpEntity(headers);

            ApiResponse apiResponse2 = restTemplate.exchange(builder.buildAndExpand(finalUrl).toUri(), GET, entity, ApiResponse.class).getBody();
            allMovies.addAll(apiResponse2.getData());
            i++;
        }


        return allMovies.stream().collect(Collectors.groupingBy(Movie::getDirector, Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > threshold).
                map(Map.Entry::getKey).sorted()
                .collect(Collectors.toList());
    }

}
