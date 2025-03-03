package com.directa24.main.challenge.service.impl;

import com.directa24.main.challenge.data.ApiResponse;
import com.directa24.main.challenge.service.MovieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Service
public class MovieServiceImpl implements MovieService {

    private final RestTemplate apiServiceRestTemplate;
    private final String apiUrl;

    public MovieServiceImpl(RestTemplate apiServiceRestTemplate,
                            @Value("${apiService.url}") final String apiUrl) {
        this.apiServiceRestTemplate = apiServiceRestTemplate;
        this.apiUrl = apiUrl;
    }

    @PostConstruct
    public void init() {
        this.getDirectors(1);
    }

    public List<String> getDirectors(int threshold) {
        final String finalUrl = apiUrl;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(finalUrl)
                // Add query parameter
                .queryParam("page", "2");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity entity = new HttpEntity(headers);

        //System.out.println(apiServiceRestTemplate.g);

        ApiResponse apiResponse = apiServiceRestTemplate.exchange(builder.buildAndExpand(finalUrl).toUri(), GET, entity, ApiResponse.class).getBody();
        System.out.println("---- : " + apiResponse.getPage());
        System.out.println("---- : " + apiResponse.getPerPage());
        System.out.println("---- : " + apiResponse.getTotal());
        System.out.println("---- : " + apiResponse.getTotalPages());
        System.out.println("---- : " + apiResponse.getData());
        apiResponse.getData().forEach(a -> {
            System.out.println("--------------------");
            System.out.println("---- : " + a.getDirector());
            System.out.println("---- : " + a.getGenre());
            System.out.println("---- : " + a.getTitle());
            System.out.println("---- : " + a.getWriter());
            System.out.println("---- : " + a.getActors());
            System.out.println("---- : " + a.getRated());
            System.out.println("---- : " + a.getRuntime());
            System.out.println("---- : " + a.getYear());
            System.out.println("--------------------");
        });
        return null;
    }

}
