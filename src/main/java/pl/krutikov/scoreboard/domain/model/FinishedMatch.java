package pl.krutikov.scoreboard.domain.model;

import lombok.Getter;
import lombok.NonNull;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.time.Instant;

@Getter
public final class FinishedMatch {

    private final MatchId id;
    private final Team homeTeam;
    private final Team awayTeam;
    private final Score score;
    private final Instant startedAt;
    private final Instant finishedAt;

    private FinishedMatch(@NonNull MatchId id,
                          @NonNull Team homeTeam,
                          @NonNull Team awayTeam,
                          @NonNull Score score,
                          @NonNull Instant startedAt,
                          @NonNull Instant finishedAt) {
        if (homeTeam.equals(awayTeam)) {
            throw new ScoreboardException(ErrorCodes.SAME_TEAM, homeTeam);
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Completion time cannot be before start time");
        }

        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static FinishedMatch from(@NonNull ActiveMatch active) {
        return new FinishedMatch(
                active.getId(),
                active.getHomeTeam(),
                active.getAwayTeam(),
                active.getScore(),
                active.getStartedAt(),
                Instant.now()
        );
    }

}
