package de.mwmrs.matchwiz.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.mwmrs.matchwiz.entity.ScoringRule;
import org.junit.jupiter.api.Test;

class ScoringServiceTest {

    private final ScoringService scoring = new ScoringService();

    private ScoringRule rule() {
        ScoringRule r = new ScoringRule();
        r.exactResultPoints = 5;
        r.goalDifferencePoints = 3;
        r.tendencyPoints = 2;
        return r;
    }

    @Test
    void exactResultScoresFive() {
        assertEquals(5, scoring.computePoints(rule(), 2, 1, 2, 1));
    }

    @Test
    void exactDrawScoresFive() {
        assertEquals(5, scoring.computePoints(rule(), 1, 1, 1, 1));
    }

    @Test
    void correctGoalDifferenceScoresThree() {
        // predicted 3:2 (diff +1), actual 2:1 (diff +1), not exact
        assertEquals(3, scoring.computePoints(rule(), 3, 2, 2, 1));
    }

    @Test
    void correctDrawDifferenceScoresThree() {
        // predicted 2:2, actual 1:1 -> both diff 0, not exact
        assertEquals(3, scoring.computePoints(rule(), 2, 2, 1, 1));
    }

    @Test
    void correctTendencyScoresTwo() {
        // predicted 3:0 (home win), actual 1:0 (home win), different diff
        assertEquals(2, scoring.computePoints(rule(), 3, 0, 1, 0));
    }

    @Test
    void wrongPredictionScoresZero() {
        // predicted away win, actual home win
        assertEquals(0, scoring.computePoints(rule(), 0, 2, 2, 0));
    }
}
