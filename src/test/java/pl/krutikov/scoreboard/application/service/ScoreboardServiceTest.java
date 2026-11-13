package pl.krutikov.scoreboard.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.domain.model.Match;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Score;
import pl.krutikov.scoreboard.domain.model.Team;
import pl.krutikov.scoreboard.domain.repository.ActiveMatchRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreboardServiceTest {

    @Mock
    private ActiveMatchRepository repository;
    @InjectMocks
    private ScoreboardService service;

    @Test
    void getSummary_ordersByTotalScoreThenMostRecentStart() {
        // Given
        final var matches = createMatches();
        final var expected = List.of("Uruguay", "Spain", "Mexico", "Argentina", "Germany");

        when(repository.findAll()).thenReturn(matches);

        // When
        final var summary = service.getSummary();
        final var actual = summary.stream()
                .map(MatchView::homeTeam)
                .toList();

        // Then
        assertEquals(expected, actual);
    }

    private List<Match> createMatches() {
        final var start = Instant.EPOCH;

        try (final var clock = mockStatic(Instant.class)) {
            clock.when(Instant::now).thenReturn(
                    start,
                    start.plusSeconds(1),
                    start.plusSeconds(2),
                    start.plusSeconds(3),
                    start.plusSeconds(4)
            );

            final var mexico = createMatch("Mexico", "Canada", 0, 5);
            final var spain = createMatch("Spain", "Brazil", 10, 2);
            final var germany = createMatch("Germany", "France", 2, 2);
            final var uruguay = createMatch("Uruguay", "Italy", 6, 6);
            final var argentina = createMatch("Argentina", "Australia", 3, 1);

            return List.of(argentina, germany, mexico, uruguay, spain);
        }
    }

    private Match createMatch(String home, String away, int homeScore, int awayScore) {
        return new Match(
                new MatchId(UUID.randomUUID()),
                Team.of(home),
                Team.of(away)
        ).updateScore(
                new Score(homeScore, awayScore)
        );
    }

}
