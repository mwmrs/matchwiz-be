package de.mwmrs.matchwiz.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class EmailServiceTest {

    @Test
    void resolvesKnownIanaZone() {
        assertEquals(ZoneId.of("Europe/Berlin"), EmailService.resolveZone("Europe/Berlin"));
    }

    @Test
    void fallsBackToUtcWhenNull() {
        assertEquals(ZoneId.of("UTC"), EmailService.resolveZone(null));
    }

    @Test
    void fallsBackToUtcWhenBlank() {
        assertEquals(ZoneId.of("UTC"), EmailService.resolveZone("  "));
    }

    @Test
    void fallsBackToUtcWhenInvalid() {
        assertEquals(ZoneId.of("UTC"), EmailService.resolveZone("not-a-real-zone"));
    }
}
