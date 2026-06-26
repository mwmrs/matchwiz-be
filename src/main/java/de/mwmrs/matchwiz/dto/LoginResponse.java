package de.mwmrs.dto;

public record LoginResponse(String token, UserDto user) {
}
