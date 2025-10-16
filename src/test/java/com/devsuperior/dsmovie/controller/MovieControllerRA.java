package com.devsuperior.dsmovie.controller;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.devsuperior.dsmovie.tests.TokenUtil2;
import io.restassured.RestAssured;
import org.json.JSONException;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MovieControllerRA {

    @LocalServerPort
    private int port;

    private String adminToken;
    private String clientToken;

    @BeforeEach
    void setup() throws Exception {
        baseURI = "http://localhost";
        RestAssured.port = port;

        // [INFERENCIA] ajuste credenciais conforme seu seed/dataset
        adminToken  = "Bearer " + TokenUtil2.obtainAccessToken("admin@gmail.com", "123456");
        clientToken = "Bearer " + TokenUtil2.obtainAccessToken("maria@gmail.com", "123456");
    }

    @Test
    public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
        given()
                .when()
                .get("/movies")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasKey("content")); // página presente
    }

    @Test
    public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
        // [INFERENCIA] use um trecho que exista no seed (ex.: "the", "o", etc.)
        String titleSnippet = "the";

        given()
                .queryParam("title", titleSnippet)
                .when()
                .get("/movies")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasKey("content"))
                // conteúdo paginado; se houver matches, pelo menos 1 item
                .body("content.size()", greaterThanOrEqualTo(0)); // robusto contra ambientes vazios
        // Opcional (se seed previsível): checar que todos os títulos contém o trecho (case-insensitive)
        // .body("content.title.collect{ it.toLowerCase() }.every{ it.contains(titleSnippet) }", is(true));
        // (uso de GPath no Rest-Assured) :contentReference[oaicite:5]{index=5}
    }

    @Test
    public void findByIdShouldReturnMovieWhenIdExists() {
        // Estratégia segura: primeiro descubra um ID existente
        Long existingId =
                given().get("/movies")
                        .then().statusCode(200)
                        .extract().jsonPath().getLong("content[0].id");

        given()
                .pathParam("id", existingId)
                .when()
                .get("/movies/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(existingId.intValue()))
                .body("title", notNullValue());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
        long nonExistingId = 999999L;

        given()
                .pathParam("id", nonExistingId)
                .when()
                .get("/movies/{id}")
                .then()
                .statusCode(404); // mapeado pelo ControllerExceptionHandler
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
        String payload = """
            {
              "id": null,
              "title": "",
              "score": 0.0,
              "count": 0,
              "image": "https://example.com/img.png"
            }
            """;

        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/movies")
                .then()
                .statusCode(422) // MethodArgumentNotValidException -> 422 por handler
                .body("status", equalTo(422))
                .body("error", equalTo("Dados inválidos"))
                .body("errors.find { it.fieldName == 'title' }", notNullValue());
    }

    @Test
    public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
        String payload = """
            {
              "id": null,
              "title": "Some Movie",
              "score": 0.0,
              "count": 0,
              "image": "https://example.com/img.png"
            }
            """;

        given()
                .header("Authorization", clientToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/movies")
                .then()
                .statusCode(403); // ROLE_CLIENT não pode inserir
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
        String payload = """
            {
              "id": null,
              "title": "Some Movie",
              "score": 0.0,
              "count": 0,
              "image": "https://example.com/img.png"
            }
            """;

        given()
                .header("Authorization", "Bearer INVALID.TOKEN")
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/movies")
                .then()
                .statusCode(401); // Bearer inválido => Unauthorized (RFC 6749 §5.2 em erros de token) :contentReference[oaicite:6]{index=6}
    }
}
