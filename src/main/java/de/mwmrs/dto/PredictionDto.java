package de.mwmrs.dto;

import de.mwmrs.entity.Prediction;
import java.time.OffsetDateTime;

public record PredictionDto(
        Long id,
        Long userId,
        Long matchId,
        Integer predictedHomeGoals,
        Integer predictedAwayGoals,
        Integer awardedPoints,
        OffsetDateTime submittedAt) {

    public static PredictionDto from(Prediction p) {
        return new PredictionDto(
                p.id,
                p.user.id,
                p.match.id,
                p.predictedHomeGoals,
                p.predictedAwayGoals,
                p.awardedPoints,
                p.submittedAt);
    }
}
