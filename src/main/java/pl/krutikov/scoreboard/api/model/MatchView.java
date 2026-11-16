package pl.krutikov.scoreboard.api.model;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record MatchView(UUID matchId,
                        String homeTeam,
                        String awayTeam,
                        int homeScore,
                        int awayScore,
                        Instant startedAt,
                        Instant finishedAt) {
}
