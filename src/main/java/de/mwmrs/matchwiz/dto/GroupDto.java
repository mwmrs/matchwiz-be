package de.mwmrs.dto;

import de.mwmrs.entity.Group;

public record GroupDto(
        Long id,
        Long competitionId,
        String name,
        String description) {

    public static GroupDto from(Group g) {
        return new GroupDto(g.id, g.competition.id, g.name, g.description);
    }
}
