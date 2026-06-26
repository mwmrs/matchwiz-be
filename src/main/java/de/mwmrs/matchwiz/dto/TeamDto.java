package de.mwmrs.dto;

import de.mwmrs.entity.Team;

public record TeamDto(
        Long id,
        String name,
        String shortName,
        String logoUrl) {

    public static TeamDto from(Team t) {
        return new TeamDto(t.id, t.name, t.shortName, t.logoUrl);
    }
}
