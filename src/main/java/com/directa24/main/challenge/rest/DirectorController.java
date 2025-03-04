package com.directa24.main.challenge.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Api(value = "/api/directors", description = "Operations for directors")
public interface DirectorController {

    // http://localhost:8081/swagger-ui/index.html#
    @Operation(summary = "Gets directors")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid threshold or missing value") })
    @GetMapping
    ResponseEntity<List<String>> getDirectors(@ApiParam(value = "Threshold value to filter the number of movies directed (greater than)", required = true) int threshold);

}
