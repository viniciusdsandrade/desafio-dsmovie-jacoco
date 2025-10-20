package com.devsuperior.dsmovie.controllers;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(PER_CLASS)
class ScoreControllerRA {

    @LocalServerPort
    private int port;

    private static final String CLIENT_ID = "myclientid";
    private static final String CLIENT_SECRET = "myclientsecret";
    private static final String CLIENT_USER = "alex@gmail.com";
    private static final String PASSWORD = "123456";

    private static final String MOVIES = "/movies";
    private static final String SCORES = "/scores";

    private String clientToken;

    private RequestSpecification base;

    @BeforeAll
    void beforeAll() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        this.base = new RequestSpecBuilder()
                .setPort(port)
                .build();

        this.clientToken = getAccessToken();
        assertIsJwt(clientToken);
    }

    private static void assertIsJwt(String token) {
        assertThat(token).as("token must be non-empty").isNotBlank();
        assertThat(token.split("\\.")).as("token must be a JWT with 3 parts").hasSize(3);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String scorePayload(Long movieId, double score) {
        if (movieId == null) {
            return """
                    { "score": %s }
                    """.formatted(score);
        }
        return """
                { "movieId": %d, "score": %s }
                """.formatted(movieId, score);
    }

    private static String getAccessToken() {
        return given()
                .auth().preemptive().basic(CLIENT_ID, CLIENT_SECRET)
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", CLIENT_USER)
                .formParam("password", PASSWORD)
                .when()
                .post("/oauth2/token")
                .then()
                .statusCode(200)
                .extract().path("access_token");
    }

    private Long anyExistingMovieId() {
        return given().spec(base)
                .when().get(MOVIES)
                .then().statusCode(200)
                .extract()
                .jsonPath()
                .getLong("content[0].id");
    }

    @Test
    void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() {
        String payload = scorePayload(999_999L, 4.0);

        given().spec(base)
                .header("Authorization", bearer(clientToken))
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(SCORES)
                .then()
                .statusCode(404);
    }

    @Test
    void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() {
        String payloadMissingMovieId = scorePayload(null, 3.5);

        given().spec(base)
                .header("Authorization", bearer(clientToken))
                .contentType(ContentType.JSON)
                .body(payloadMissingMovieId)
                .when()
                .put(SCORES)
                .then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("error", equalTo("Dados inválidos"))
                .body("errors.find { it.fieldName == 'movieId' }", notNullValue());
    }

    @Test
    void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() {
        Long existingId = anyExistingMovieId();
        String payloadNegativeScore = scorePayload(existingId, -1.0);

        given().spec(base)
                .header("Authorization", bearer(clientToken))
                .contentType(ContentType.JSON)
                .body(payloadNegativeScore)
                .when()
                .put(SCORES)
                .then()
                .statusCode(422)
                .body("errors.find { it.fieldName == 'score' }", notNullValue());
    }
}
