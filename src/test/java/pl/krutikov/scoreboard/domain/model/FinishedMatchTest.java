package pl.krutikov.scoreboard.domain.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FinishedMatchTest {


    @Test
    void from_rejectsFinishBeforeStart() {
        // Given
        final var start = Instant.EPOCH;
        final var finish = start.minusSeconds(1);

        try (final var clock = Mockito.mockStatic(Instant.class)) {
            clock.when(Instant::now).thenReturn(start, finish);

            final var active = new ActiveMatch(
                    new MatchId(UUID.randomUUID()),
                    Team.of("Mexico"),
                    Team.of("Canada")
            );

            // When, Then
            assertThrows(IllegalArgumentException.class, () -> FinishedMatch.from(active));
        }
    }

}
