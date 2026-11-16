package pl.krutikov.scoreboard.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryTest {

    @Test
    void find_ignoresDefaultLocale() {
        // Given
        final var original = Locale.getDefault();

        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            // When
            final var italy = Country.find("italy");
            final var unitedStates = Country.find("UNITED STATES");

            // Then
            assertAll(
                    () -> assertEquals(Country.ITALY, italy.orElseThrow()),
                    () -> assertEquals(Country.UNITED_STATES, unitedStates.orElseThrow())
            );
        } finally {
            Locale.setDefault(original);
        }
    }

}
