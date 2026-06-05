package de.mwmrs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * End-to-end smoke test of the prediction spine:
 * login -> competition -> teams/matchday/match -> register+approve user+login+join group
 * +approve membership -> submit prediction -> enter result (scoring) -> rankings.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MatchWizSmokeTest {

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private String login(String username, String password) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void fullPredictionFlow() {
        String admin = login("admin", "admin");

        Long competitionId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("name", "Bundesliga", "season", "2026/27"))
                .when().post("/api/competitions")
                .then().statusCode(201).extract().path("id")).longValue();

        Long teamA = createTeam(admin, "Team A", "TA");
        Long teamB = createTeam(admin, "Team B", "TB");

        Long matchdayId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("competitionId", competitionId, "number", 1))
                .when().post("/api/matchdays")
                .then().statusCode(201).extract().path("id")).longValue();

        Long matchId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("homeTeamId", teamA, "awayTeamId", teamB,
                        "kickoffTime", OffsetDateTime.now().plusDays(1).toString()))
                .when().post("/api/matchdays/" + matchdayId + "/matches")
                .then().statusCode(201).extract().path("id")).longValue();

        Long groupId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("competitionId", competitionId, "name", "Family"))
                .when().post("/api/groups")
                .then().statusCode(201).extract().path("id")).longValue();

        // Register a member (created inactive); login blocked until approved.
        Long memberId = ((Number) given().contentType(ContentType.JSON)
                .body(Map.of("username", "member1", "password", "pw"))
                .when().post("/api/auth/register")
                .then().statusCode(201)
                .body("active", equalTo(false))
                .extract().path("id")).longValue();

        given().contentType(ContentType.JSON)
                .body(Map.of("username", "member1", "password", "pw"))
                .when().post("/api/auth/login")
                .then().statusCode(403);

        // Admin approves the user account.
        given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .when().post("/api/users/" + memberId + "/approve")
                .then().statusCode(200).body("active", equalTo(true));

        String member = login("member1", "pw");

        // Member joins group (pending) -> group admin approves.
        given().contentType(ContentType.JSON).header("Authorization", bearer(member))
                .when().post("/api/groups/" + groupId + "/join")
                .then().statusCode(201).body("approved", equalTo(false));

        // First approved member is promoted to GROUP_ADMIN.
        given().header("Authorization", bearer(admin))
                .when().post("/api/groups/" + groupId + "/members/" + memberId + "/approve")
                .then().statusCode(200)
                .body("approved", equalTo(true))
                .body("role", equalTo("GROUP_ADMIN"));

        // The promoted member (NOT the global admin) can use a group-admin endpoint.
        given().header("Authorization", bearer(member))
                .when().get("/api/groups/" + groupId + "/members")
                .then().statusCode(200);

        // Member submits an exact prediction (returns 200 - upsert).
        given().contentType(ContentType.JSON).header("Authorization", bearer(member))
                .body(List.of(Map.of("matchId", matchId, "predictedHomeGoals", 2, "predictedAwayGoals", 1)))
                .when().post("/api/matchdays/" + matchdayId + "/predictions?groupId=" + groupId)
                .then().statusCode(200);

        // Admin enters the final result -> triggers scoring.
        given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("homeGoals", 2, "awayGoals", 1, "status", "FINISHED"))
                .when().patch("/api/matches/" + matchId)
                .then().statusCode(200);

        // Prediction now carries the awarded points (exact = 5).
        given().header("Authorization", bearer(member))
                .when().get("/api/matchdays/" + matchdayId + "/predictions?groupId=" + groupId)
                .then().statusCode(200)
                .body("[0].awardedPoints", equalTo(5));

        // Ranking reflects the points.
        given().header("Authorization", bearer(member))
                .when().get("/api/groups/" + groupId + "/rankings")
                .then().statusCode(200)
                .body("[0].username", equalTo("member1"))
                .body("[0].totalPoints", equalTo(5))
                .body("[0].exactPredictions", equalTo(1))
                .body("[0].rank", equalTo(1));
    }

    @Test
    void predictionAfterKickoffRejected() {
        String admin = login("admin", "admin");

        Long competitionId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("name", "LaLiga", "season", "2026/27"))
                .when().post("/api/competitions")
                .then().statusCode(201).extract().path("id")).longValue();

        Long teamA = createTeam(admin, "Real", "RMA");
        Long teamB = createTeam(admin, "Barca", "FCB");

        Long matchdayId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("competitionId", competitionId, "number", 1))
                .when().post("/api/matchdays")
                .then().statusCode(201).extract().path("id")).longValue();

        Long matchId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("homeTeamId", teamA, "awayTeamId", teamB,
                        "kickoffTime", OffsetDateTime.now().minusHours(1).toString()))
                .when().post("/api/matchdays/" + matchdayId + "/matches")
                .then().statusCode(201).extract().path("id")).longValue();

        Long groupId = ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .body(Map.of("competitionId", competitionId, "name", "LaLiga Tipprunde"))
                .when().post("/api/groups")
                .then().statusCode(201).extract().path("id")).longValue();

        Long memberId = ((Number) given().contentType(ContentType.JSON)
                .body(Map.of("username", "laMember", "password", "pw"))
                .when().post("/api/auth/register")
                .then().statusCode(201).extract().path("id")).longValue();

        given().contentType(ContentType.JSON).header("Authorization", bearer(admin))
                .when().post("/api/users/" + memberId + "/approve")
                .then().statusCode(200);

        String member = login("laMember", "pw");

        given().contentType(ContentType.JSON).header("Authorization", bearer(member))
                .when().post("/api/groups/" + groupId + "/join")
                .then().statusCode(201);

        given().header("Authorization", bearer(admin))
                .when().post("/api/groups/" + groupId + "/members/" + memberId + "/approve")
                .then().statusCode(200);

        given().contentType(ContentType.JSON).header("Authorization", bearer(member))
                .body(List.of(Map.of("matchId", matchId, "predictedHomeGoals", 1, "predictedAwayGoals", 0)))
                .when().post("/api/matchdays/" + matchdayId + "/predictions?groupId=" + groupId)
                .then().statusCode(400);
    }

    @Test
    void unauthenticatedRequestsAreRejected() {
        given().when().get("/api/competitions").then().statusCode(401);
    }

    private Long createTeam(String adminToken, String name, String shortName) {
        return ((Number) given().contentType(ContentType.JSON).header("Authorization", bearer(adminToken))
                .body(Map.of("name", name, "shortName", shortName))
                .when().post("/api/teams")
                .then().statusCode(201).extract().path("id")).longValue();
    }
}
