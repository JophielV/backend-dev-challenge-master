package com.directa24.main.challenge;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Configuration
public class WebServiceConfiguration {

    @Value("${apiService.readTimeoutMillis}")
    private Integer readTimeoutMillis;
    @Value("${apiService.connectionTimeoutMillis}")
    private Integer connectionTimeoutMillis;

    @Bean
    public RestTemplate restTemplate(final MappingJackson2HttpMessageConverter messageConverter) {
        return new RestTemplateBuilder()
                .setReadTimeout(Duration.ofMillis(readTimeoutMillis))
                .setConnectTimeout(Duration.ofMillis(connectionTimeoutMillis))
                .messageConverters(messageConverter)
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
}