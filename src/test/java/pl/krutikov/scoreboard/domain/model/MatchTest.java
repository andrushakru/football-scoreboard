package pl.krutikov.scoreboard.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchTest {

    @ParameterizedTest
    @MethodSource("identicalTeams")
    void match_rejectsIdenticalTeams(String home, String away) {
        // When
        final var exception = assertThrows(ScoreboardException.class, () -> match(home, away));

        // Then
        assertEquals(ErrorCodes.SAME_TEAM, exception.getCode());
    }

    @ParameterizedTest
    @MethodSource("opponents")
    void conflicts_detectsSharedTeams(String home, String away, boolean expected) {
        // Given
        final var existing = match("Brazil", "Mexico");
        final var candidate = match(home, away);

        // When
        final var conflicts = existing.conflicts(candidate);

        // Then
        assertEquals(expected, conflicts);
    }

    private static Stream<Arguments> identicalTeams() {
        return Stream.of(
                Arguments.of("Canada", "Canada"),
                Arguments.of("Canada", " CANADA ")
        );
    }

    private static Stream<Arguments> opponents() {
        return Stream.of(
                Arguments.of("Brazil", "Canada", true),
                Arguments.of("Canada", "Brazil", true),
                Arguments.of("Mexico", "Canada", true),
                Arguments.of("Canada", "Mexico", true),
                Arguments.of("France", "Germany", false)
        );
    }

    private static Match match(String home, String away) {
        return new Match(
                new MatchId(UUID.randomUUID()),
                Team.of(home),
                Team.of(away)
        );
    }

}
