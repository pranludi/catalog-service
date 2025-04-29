package com.polarbookshop.catalogservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.polarbookshop.catalogservice.domain.Book;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers
class CatalogServiceApplicationTests {

    static KeycloakToken bjornTokens;
    static KeycloakToken isabelleTokens;

    @Autowired
    WebTestClient webTestClient;

    @Container
    static final KeycloakContainer keycloakContainer =
        new KeycloakContainer("quay.io/keycloak/keycloak:latest")
            .withRealmImportFile("test-realm-config.json");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.security.oauth2.resourceserver.jwt.issuer-uri",
            () -> keycloakContainer.getAuthServerUrl() + "/realms/PolarBookshop"
        );
    }

    @BeforeAll
    static void generateAccessTokens() {
        WebClient webClient = WebClient.builder()
            .baseUrl(
                keycloakContainer.getAuthServerUrl()
                    + "/realms/PolarBookshop/protocol/openid-connect/token"
            )
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

        isabelleTokens = authenticateWith("isabelle", "password", webClient);
        bjornTokens = authenticateWith("bjorn", "password", webClient);
    }

    @Test
    void whenPostRequestThenBookCreated() {
        var expectedBook = Book.of("1231231231", "Title", "Author", 9.90, "publisher");

        webTestClient
            .post()
            .uri("/books")
            .headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
            .bodyValue(expectedBook)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Book.class).value(actualBook -> {
                assertThat(actualBook).isNotNull();
                assertThat(actualBook.isbn()).isEqualTo(expectedBook.isbn());
            });
    }

    @Test
    void whenPostRequestUnauthorizedThen403() {
        var expectedBook = Book.of("1231231231", "Title", "Author", 9.90, "publisher");

        webTestClient.post().uri("/books")
            .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
            .bodyValue(expectedBook)
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void whenPostRequestUnauthorizedThen401() {
        var expectedBook = Book.of("1231231231", "Title", "Author", 9.90, "publisher");

        webTestClient.post().uri("/books")
            .bodyValue(expectedBook)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
        return webClient
            .post()
            .body(
                BodyInserters.fromFormData("grant_type", "password")
                    .with("client_id", "polar-test")
                    .with("username", username)
                    .with("password", password)
            )
            .retrieve()
            .bodyToMono(KeycloakToken.class)
            .block();
    }

    private record KeycloakToken(String accessToken) {

        @JsonCreator
        private KeycloakToken(
            @JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }
    }

}
