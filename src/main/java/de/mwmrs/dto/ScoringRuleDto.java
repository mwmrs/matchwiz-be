package de.mwmrs.dto;

import de.mwmrs.entity.ScoringRule;

public record ScoringRuleDto(
        Long competitionId,
        int exactResultPoints,
        int goalDifferencePoints,
        int tendencyPoints) {

    public static ScoringRuleDto from(ScoringRule r) {
        return new ScoringRuleDto(
                r.competition.id,
                r.exactResultPoints,
                r.goalDifferencePoints,
                r.tendencyPoints);
    }
}
