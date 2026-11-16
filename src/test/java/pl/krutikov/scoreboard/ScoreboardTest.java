package pl.krutikov.scoreboard;

import org.junit.jupiter.api.Test;
import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.api.command.UpdateScoreCommand;
import pl.krutikov.scoreboard.api.query.GetHistoryQuery;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardTest {

    @Test
    void create_returnsIndependentScoreboards() {
        // Given
        final var first = Scoreboards.create();
        final var second = Scoreboards.create();

        // When
        final var firstMatch = first.startMatch(start("Mexico", "Canada"));
        final var secondMatch = second.startMatch(start("Mexico", "Canada"));

        // Then
        assertAll(
                () -> assertEquals(List.of(firstMatch), first.getSummary()),
                () -> assertEquals(List.of(secondMatch), second.getSummary())
        );
    }

    @Test
    void create_usesConfiguredCapacity() {
        // Given
        final var scoreboard = Scoreboards.create(2);
        final var first = scoreboard.startMatch(start("Mexico", "Canada"));
        final var second = scoreboard.startMatch(start("Brazil", "France"));
        final var third = scoreboard.startMatch(start("Spain", "Germany"));

        // When
        scoreboard.finishMatch(finish(first.matchId()));
        final var secondFinished = scoreboard.finishMatch(finish(second.matchId()));
        final var thirdFinished = scoreboard.finishMatch(finish(third.matchId()));
        final var history = scoreboard.getHistory(new GetHistoryQuery(3));

        // Then
        assertAll(
                () -> assertEquals(2, history.size()),
                () -> assertTrue(history.containsAll(List.of(secondFinished, thirdFinished)))
        );
    }

    @Test
    void start_initializesMatch() {
        // Given
        final var scoreboard = Scoreboards.create();

        // When
        final var match = scoreboard.startMatch(start("Mexico", "Canada"));

        // Then
        assertAll(
                () -> assertEquals(0, match.homeScore()),
                () -> assertEquals(0, match.awayScore()),
                () -> assertEquals(List.of(match), scoreboard.getSummary())
        );
    }

    @Test
    void start_rejectsOccupiedTeam() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var existing = scoreboard.startMatch(start("Mexico", "Canada"));

        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> scoreboard.startMatch(start("Brazil", "Mexico"))
        );

        // Then
        assertAll(
                () -> assertEquals(ErrorCodes.TEAM_ALREADY_PLAYING, exception.getCode()),
                () -> assertEquals(List.of(existing), scoreboard.getSummary())
        );
    }

    @Test
    void start_rejectsUnsupportedTeam() {
        // Given
        final var scoreboard = Scoreboards.create();

        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> scoreboard.startMatch(start("Atlantis", "Canada"))
        );

        // Then
        assertEquals(ErrorCodes.UNSUPPORTED_TEAM, exception.getCode());
    }

    @Test
    void update_overwritesScore() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var match = scoreboard.startMatch(start("Mexico", "Canada"));
        scoreboard.updateScore(update(match.matchId(), 3, 2));

        // When
        final var actual = scoreboard.updateScore(update(match.matchId(), 1, 0));

        // Then
        assertAll(
                () -> assertEquals(1, actual.homeScore()),
                () -> assertEquals(0, actual.awayScore()),
                () -> assertEquals(List.of(actual), scoreboard.getSummary())
        );
    }

    @Test
    void update_rejectsInvalidScore() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var match = scoreboard.startMatch(start("Mexico", "Canada"));
        final var previous = scoreboard.updateScore(update(match.matchId(), 2, 1));

        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> scoreboard.updateScore(update(match.matchId(), -1, 0))
        );

        // Then
        assertAll(
                () -> assertEquals(ErrorCodes.INVALID_SCORE, exception.getCode()),
                () -> assertEquals(List.of(previous), scoreboard.getSummary())
        );
    }

    @Test
    void update_rejectsFinishedMatch() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var match = scoreboard.startMatch(start("Mexico", "Canada"));
        scoreboard.finishMatch(finish(match.matchId()));

        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> scoreboard.updateScore(update(match.matchId(), 1, 0))
        );

        // Then
        assertEquals(ErrorCodes.MATCH_NOT_FOUND, exception.getCode());
    }

    @Test
    void finish_releasesTeams() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var match = scoreboard.startMatch(start("Mexico", "Canada"));

        // When
        scoreboard.finishMatch(finish(match.matchId()));
        final var next = scoreboard.startMatch(start("Canada", "Mexico"));

        // Then
        assertEquals(List.of(next), scoreboard.getSummary());
    }

    @Test
    void finish_savesMatchToHistory() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var started = scoreboard.startMatch(new StartMatchCommand("Mexico", "Canada"));
        scoreboard.updateScore(update(started.matchId(), 3, 2));

        // When
        final var finished = scoreboard.finishMatch(new FinishMatchCommand(started.matchId()));
        final var history = scoreboard.getHistory(new GetHistoryQuery(100));

        // Then
        assertAll(
                () -> assertEquals(3, finished.homeScore()),
                () -> assertEquals(2, finished.awayScore()),
                () -> assertEquals(List.of(finished), history)
        );
    }

    @Test
    void finish_rejectsUnknownMatch() {
        // Given
        final var scoreboard = Scoreboards.create();
        final var unknownId = UUID.randomUUID();

        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> scoreboard.finishMatch(finish(unknownId))
        );

        // Then
        assertEquals(ErrorCodes.MATCH_NOT_FOUND, exception.getCode());
    }

    @Test
    void getHistory_rejectsNegativeLimits() {
        // Given
        final var scoreboard = Scoreboards.create();

        // When
        final var exception = assertThrows(
                ScoreboardException.class,
                () -> scoreboard.getHistory(new GetHistoryQuery(-1))
        );

        // Then
        assertEquals(ErrorCodes.INVALID_HISTORY_LIMIT, exception.getCode());
    }

    @Test
    void getHistory_returnsEmptyHistoryWhenNoMatchesFinished() {
        // Given
        final var scoreboard = Scoreboards.create();
        scoreboard.startMatch(new StartMatchCommand("Mexico", "Canada"));

        // When
        final var history = scoreboard.getHistory(new GetHistoryQuery(1));

        // Then
        assertEquals(List.of(), history);
    }

    private static StartMatchCommand start(String home, String away) {
        return StartMatchCommand.builder()
                .homeTeam(home)
                .awayTeam(away)
                .build();
    }

    private static UpdateScoreCommand update(UUID id, int home, int away) {
        return UpdateScoreCommand.builder()
                .matchId(id)
                .homeScore(home)
                .awayScore(away)
                .build();
    }

    private static FinishMatchCommand finish(UUID id) {
        return FinishMatchCommand.builder()
                .matchId(id)
                .build();
    }

}
