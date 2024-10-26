package br.dev.ampliar.caipora;

import br.dev.ampliar.caipora.config.BaseIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


class CaiporaApplicationTest extends BaseIT {

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

    @Test
    void springSessionWorks() {
        RestAssured
                .given()
                    .sessionId(webFormsSession(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .get("/sessionCreate")
                .then()
                    .statusCode(HttpStatus.OK.value());
        RestAssured
                .given()
                    .sessionId(webFormsSession(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .get("/sessionRead")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(Matchers.equalTo("test"));
    }

}
