package de.mwmrs.dto;

public record RankingEntryDto(
        int rank,
        Long userId,
        String username,
        int totalPoints,
        int exactPredictions,
        int goalDifferencePredictions,
        int tendencyPredictions) {
}
