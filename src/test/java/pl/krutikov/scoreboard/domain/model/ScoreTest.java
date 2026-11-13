package pl.krutikov.scoreboard.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreTest {

    @ParameterizedTest
    @MethodSource("validScores")
    void constructor_acceptsScoresWithinInclusiveRange(int home, int away, int expectedTotal) {
        // When
        final var score = new Score(home, away);

        // Then
        assertEquals(expectedTotal, score.total());
    }

    @ParameterizedTest
    @MethodSource("invalidScores")
    void constructor_rejectsScoresOutsideRange(int home, int away) {
        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> new Score(home, away)
        );

        // Then
        assertEquals(ErrorCodes.INVALID_SCORE, exception.getCode());
    }

    private static Stream<Arguments> validScores() {
        return Stream.of(
                Arguments.of(0, 0, 0),
                Arguments.of(3, 2, 5),
                Arguments.of(50, 0, 50),
                Arguments.of(0, 50, 50),
                Arguments.of(50, 50, 100)
        );
    }

    private static Stream<Arguments> invalidScores() {
        return Stream.of(
                Arguments.of(-1, 0),
                Arguments.of(0, -1),
                Arguments.of(51, 0),
                Arguments.of(0, 51)
        );
    }

}
