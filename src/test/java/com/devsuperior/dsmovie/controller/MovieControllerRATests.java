package com.devsuperior.dsmovie.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(PER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MovieControllerRATests {

    @LocalServerPort
    private int port;

    private static final String CLIENT_ID = "myclientid";
    private static final String CLIENT_SECRET = "myclientsecret";
    private static final String ADMIN_USER = "maria@gmail.com";
    private static final String CLIENT_USER = "alex@gmail.com";
    private static final String PASSWORD = "123456";

    private static final String MOVIES = "/movies";

    private String adminToken;
    private String clientToken;

    private RequestSpecification base;

    @BeforeAll
    void beforeAllInitServerAndTokens() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        this.base = new RequestSpecBuilder()
                .setPort(port)
                .build();

        this.adminToken = getAccessToken(ADMIN_USER);
        this.clientToken = getAccessToken(CLIENT_USER);

        assertIsJwt(adminToken);
        assertIsJwt(clientToken);
    }

    private static void assertIsJwt(String token) {
        assertThat(token).as("token must be non-empty").isNotBlank();
        assertThat(token.split("\\.")).as("token must be a JWT with 3 parts").hasSize(3);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String moviePayload(String title) {
        return """
                {
                  "id": null,
                  "title": "%s",
                  "score": %s,
                  "count": %s,
                  "image": "%s"
                }
                """.formatted(title, 0.0, 0, "https://example.com/img.png");
    }

    private static String getAccessToken(String username) {
        return given()
                .auth()
                .preemptive()
                .basic(CLIENT_ID, CLIENT_SECRET)
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", username)
                .formParam("password", PASSWORD)
                .when()
                .post("/oauth2/token")
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }

    private Long anyExistingMovieId() {
        return given().spec(base)
                .when().get(MOVIES)
                .then().statusCode(200)
                .extract().jsonPath().getLong("content[0].id");
    }

    @Test
    void findAllShouldReturnOkWhenNoArgumentsGiven() {
        given().spec(base)
                .when()
                .get(MOVIES)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("$", hasKey("content"));
    }

    @Test
    void findAllShouldReturnPagedMoviesWhenTitleParamNotEmpty() {
        String titleSnippet = "the";

        given().spec(base)
                .queryParam("title", titleSnippet)
                .when()
                .get(MOVIES)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("$", hasKey("content"))
                .body("content.size()", greaterThanOrEqualTo(0));
    }

    @Test
    void findByIdShouldReturnMovieWhenIdExists() {
        Long existingId = anyExistingMovieId();

        given().spec(base)
                .pathParam("id", existingId)
                .when()
                .get(MOVIES + "/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(existingId.intValue()))
                .body("title", notNullValue());
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
        long nonExistingId = 999_999L;

        given().spec(base)
                .pathParam("id", nonExistingId)
                .when()
                .get(MOVIES + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() {
        String payload = moviePayload("");

        given().spec(base)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(payload)
                .when()
                .post(MOVIES)
                .then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("error", equalTo("Dados inválidos"))
                .body("errors.find { it.fieldName == 'title' }", notNullValue());
    }

    @Test
    void insertShouldReturnForbiddenWhenClientLogged() {
        String payload = moviePayload("Some Movie");

        given().spec(base)
                .header("Authorization", bearer(clientToken))
                .contentType(JSON)
                .body(payload)
                .when()
                .post(MOVIES)
                .then()
                .statusCode(403);
    }

    @Test
    void insertShouldReturnUnauthorizedWhenInvalidToken() {
        String payload = moviePayload("Some Movie");

        given().spec(base)
                .header("Authorization", "Bearer INVALID.TOKEN")
                .contentType(JSON)
                .body(payload)
                .when()
                .post(MOVIES)
                .then()
                .statusCode(401);
    }
}
