package com.devsuperior.dsmovie.controller;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.devsuperior.dsmovie.tests.TokenUtil2;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScoreControllerRA {

    @LocalServerPort
    private int port;

    private String clientToken;

    @BeforeEach
    void setup() throws Exception {
        baseURI = "http://localhost";
        RestAssured.port = port;
        clientToken = "Bearer " + TokenUtil2.obtainAccessToken("maria@gmail.com", "123456");
    }

    @Test
    public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {
        String payload = """
            { "movieId": 999999, "score": 4.0 }
            """;

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put("/scores")
                .then()
                .statusCode(404);
    }

    @Test
    public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {
        String payloadMissingMovieId = """
            { "score": 3.5 }
            """;

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(payloadMissingMovieId)
                .when()
                .put("/scores")
                .then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("error", equalTo("Dados inválidos"))
                .body("errors.find { it.fieldName == 'movieId' }", notNullValue());
    }

    @Test
    public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {
        Long existingId =
                given().get("/movies").then().statusCode(200).extract().jsonPath().getLong("content[0].id");

        String payloadNegativeScore = """
            { "movieId": %d, "score": -1.0 }
            """.formatted(existingId);

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(payloadNegativeScore)
                .when()
                .put("/scores")
                .then()
                .statusCode(422)
                .body("errors.find { it.fieldName == 'score' }", notNullValue());
    }
}
