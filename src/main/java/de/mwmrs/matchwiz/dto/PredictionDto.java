package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.Prediction;
import java.time.OffsetDateTime;

public record PredictionDto(
        Long id,
        Long userId,
        Long groupId,
        Long matchId,
        Integer predictedHomeGoals,
        Integer predictedAwayGoals,
        Integer awardedPoints,
        OffsetDateTime submittedAt) {

    public static PredictionDto from(Prediction p) {
        return new PredictionDto(
                p.id,
                p.user.id,
                p.group.id,
                p.match.id,
                p.predictedHomeGoals,
                p.predictedAwayGoals,
                p.awardedPoints,
                p.submittedAt);
    }
}
