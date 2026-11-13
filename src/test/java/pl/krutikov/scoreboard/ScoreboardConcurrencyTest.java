package pl.krutikov.scoreboard;

import org.junit.jupiter.api.RepeatedTest;
import pl.krutikov.scoreboard.api.Scoreboard;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ScoreboardConcurrencyTest {

    @RepeatedTest(50)
    void startMatch_rejectsConcurrentMatchesWithSharedTeam() throws Exception {
        // Given
        final var scoreboard = Scoreboards.create();
        final var executor = Executors.newFixedThreadPool(2);
        final var start = new CompletableFuture<Void>();

        try {
            // When
            final var first = start.thenApplyAsync(
                    ignored -> startMatch(scoreboard, "Mexico", "Canada"),
                    executor
            );
            final var second = start.thenApplyAsync(
                    ignored -> startMatch(scoreboard, "Brazil", "Mexico"),
                    executor
            );

            start.complete(null);

            final var firstResult = first.get(5, TimeUnit.SECONDS);
            final var secondResult = second.get(5, TimeUnit.SECONDS);

            // Then
            assertAll(
                    () -> assertNotEquals(firstResult, secondResult),
                    () -> assertEquals(1, scoreboard.getSummary().size())
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private StartResult startMatch(Scoreboard scoreboard, String home, String away) {
        try {
            scoreboard.startMatch(StartMatchCommand.builder()
                    .homeTeam(home)
                    .awayTeam(away)
                    .build());

            return StartResult.STARTED;
        } catch (ScoreboardException exception) {
            return StartResult.REJECTED;
        }
    }

    private enum StartResult {
        STARTED,
        REJECTED
    }

}
