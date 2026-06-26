package de.mwmrs.matchwiz;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

/**
 * End-to-end test of the password reset flow: request a code by email
 * (captured via the mock mailer), confirm with code + new password, and
 * verify single-use / no-leak semantics.
 */
@QuarkusTest
class PasswordResetFlowTest {

    private static final Pattern CODE_PATTERN = Pattern.compile("reset code: ([A-Z2-9]{8})");

    @Inject
    MockMailbox mailbox;

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void clearMailbox() {
        mailbox.clear();
    }

    @Test
    void fullPasswordResetFlow() {
        String email = "resetter@example.com";
        registerAndApprove("resetter", "oldpw", email);

        // Request a reset code; the mock mailer captures the mail.
        given().contentType(ContentType.JSON)
                .body(Map.of("email", email))
                .when().post("/api/auth/password-reset/request")
                .then().statusCode(204);

        List<Mail> mails = mailbox.getMailsSentTo(email);
        assertEquals(1, mails.size());
        String code = extractCode(mails.get(0).getText());

        // Confirm with the code (lowercase + whitespace tolerated).
        given().contentType(ContentType.JSON)
                .body(Map.of("code", " " + code.toLowerCase() + " ", "newPassword", "newpw"))
                .when().post("/api/auth/password-reset/confirm")
                .then().statusCode(204);

        // New password works, old one does not.
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "resetter", "password", "newpw"))
                .when().post("/api/auth/login")
                .then().statusCode(200);
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "resetter", "password", "oldpw"))
                .when().post("/api/auth/login")
                .then().statusCode(401);

        // The code is single-use.
        given().contentType(ContentType.JSON)
                .body(Map.of("code", code, "newPassword", "anotherpw"))
                .when().post("/api/auth/password-reset/confirm")
                .then().statusCode(400);
    }

    @Test
    void invalidCodeRejected() {
        given().contentType(ContentType.JSON)
                .body(Map.of("code", "WRONGCOD", "newPassword", "whatever"))
                .when().post("/api/auth/password-reset/confirm")
                .then().statusCode(400);
    }

    @Test
    void unknownEmailDoesNotLeak() {
        given().contentType(ContentType.JSON)
                .body(Map.of("email", "nobody@example.com"))
                .when().post("/api/auth/password-reset/request")
                .then().statusCode(204);
        assertEquals(0, mailbox.getTotalMessagesSent());
    }

    @Test
    void requestingNewCodeInvalidatesPreviousOne() {
        String email = "twice@example.com";
        registerAndApprove("twice", "pw", email);

        given().contentType(ContentType.JSON)
                .body(Map.of("email", email))
                .when().post("/api/auth/password-reset/request")
                .then().statusCode(204);
        given().contentType(ContentType.JSON)
                .body(Map.of("email", email))
                .when().post("/api/auth/password-reset/request")
                .then().statusCode(204);

        List<Mail> mails = mailbox.getMailsSentTo(email);
        assertEquals(2, mails.size());
        String firstCode = extractCode(mails.get(0).getText());
        String secondCode = extractCode(mails.get(1).getText());

        given().contentType(ContentType.JSON)
                .body(Map.of("code", firstCode, "newPassword", "newpw"))
                .when().post("/api/auth/password-reset/confirm")
                .then().statusCode(400);
        given().contentType(ContentType.JSON)
                .body(Map.of("code", secondCode, "newPassword", "newpw"))
                .when().post("/api/auth/password-reset/confirm")
                .then().statusCode(204);
    }

    private void registerAndApprove(String username, String password, String email) {
        Long userId = ((Number) given().contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password, "email", email))
                .when().post("/api/auth/register")
                .then().statusCode(201).extract().path("id")).longValue();

        String admin = given().contentType(ContentType.JSON)
                .body(Map.of("username", "admin", "password", "admin"))
                .when().post("/api/auth/login")
                .then().statusCode(200).extract().path("token");

        given().contentType(ContentType.JSON).header("Authorization", "Bearer " + admin)
                .when().post("/api/users/" + userId + "/approve")
                .then().statusCode(200);
    }

    private String extractCode(String mailText) {
        Matcher matcher = CODE_PATTERN.matcher(mailText);
        assertNotNull(mailText);
        if (!matcher.find()) {
            throw new AssertionError("No reset code found in mail body:\n" + mailText);
        }
        return matcher.group(1);
    }
}
