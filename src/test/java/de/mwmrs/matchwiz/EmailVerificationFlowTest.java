package de.mwmrs.matchwiz;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EmailVerificationFlowTest {

    private static final Pattern CODE_PATTERN = Pattern.compile("verification code: ([A-Z2-9]{8})");

    private static String adminToken;

    @Inject
    MockMailbox mailbox;

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        adminToken = given().contentType(ContentType.JSON)
                .body(Map.of("username", "admin", "password", "admin"))
                .when().post("/api/auth/login")
                .then().statusCode(200).extract().path("token");
    }

    @BeforeEach
    void clearMailbox() {
        mailbox.clear();
    }

    @Test
    void fullEmailVerificationFlow() {
        String email = "verify@example.com";
        String token = registerAndApprove("verifyuser", "pw", email);

        assertFalse(emailVerified(token));

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(204);

        List<Mail> mails = mailbox.getMailsSentTo(email);
        assertEquals(1, mails.size());
        String code = extractCode(mails.get(0).getText());

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", code))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(204);

        assertTrue(emailVerified(token));
    }

    @Test
    void invalidCodeRejected() {
        String token = registerAndApprove("verifybad", "pw", "verifybad@example.com");

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(204);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", "WRONGCOD"))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(400);
    }

    @Test
    void noEmailOnAccountReturnsBadRequest() {
        String token = registerAndApprove("noemail", "pw", null);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(400);
    }

    @Test
    void codeIsSingleUse() {
        String email = "singleuse@example.com";
        String token = registerAndApprove("singleuse", "pw", email);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(204);

        String code = extractCode(mailbox.getMailsSentTo(email).get(0).getText());

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", code))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(204);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", code))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(400);
    }

    @Test
    void requestingNewCodeInvalidatesPreviousOne() {
        String email = "twiceverify@example.com";
        String token = registerAndApprove("twiceverify", "pw", email);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(204);
        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(204);

        List<Mail> mails = mailbox.getMailsSentTo(email);
        assertEquals(2, mails.size());
        String firstCode = extractCode(mails.get(0).getText());
        String secondCode = extractCode(mails.get(1).getText());

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", firstCode))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(400);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", secondCode))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(204);
    }

    @Test
    void emailChangeResetsVerifiedFlag() {
        String email = "changeme@example.com";
        String token = registerAndApprove("changeme", "pw", email);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(204);
        String code = extractCode(mailbox.getMailsSentTo(email).get(0).getText());
        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("code", code))
                .when().post("/api/auth/verify-email/confirm")
                .then().statusCode(204);

        assertTrue(emailVerified(token));

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("email", "newemail@example.com"))
                .when().patch("/api/users/me")
                .then().statusCode(200);

        assertFalse(emailVerified(token));
    }

    @Test
    void unauthenticatedRequestRejected() {
        given().contentType(ContentType.JSON)
                .when().post("/api/auth/verify-email/request")
                .then().statusCode(401);
    }

    private boolean emailVerified(String token) {
        return Boolean.TRUE.equals(given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/users/me")
                .then().statusCode(200)
                .extract().<Boolean>path("emailVerified"));
    }

    private String registerAndApprove(String username, String password, String email) {
        Map<String, Object> body = email != null
                ? Map.of("username", username, "password", password, "email", email)
                : Map.of("username", username, "password", password);

        Long userId = ((Number) given().contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/register")
                .then().statusCode(201).extract().path("id")).longValue();

        given().contentType(ContentType.JSON).header("Authorization", "Bearer " + adminToken)
                .when().post("/api/users/" + userId + "/approve")
                .then().statusCode(200);

        return given().contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .when().post("/api/auth/login")
                .then().statusCode(200).extract().path("token");
    }

    private String extractCode(String mailText) {
        assertNotNull(mailText);
        Matcher matcher = CODE_PATTERN.matcher(mailText);
        if (!matcher.find()) {
            throw new AssertionError("No verification code found in mail body:\n" + mailText);
        }
        return matcher.group(1);
    }
}
