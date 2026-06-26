package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.Group;

public record GroupDto(
        Long id,
        Long competitionId,
        String name,
        String description) {

    public static GroupDto from(Group g) {
        return new GroupDto(g.id, g.competition.id, g.name, g.description);
    }
}
