package pl.krutikov.scoreboard.domain.model;

import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActiveMatch {

    private final MatchId id;
    private final Team homeTeam;
    private final Team awayTeam;
    private final Instant startedAt;
    private volatile Score score;

    public ActiveMatch(@NonNull MatchId id, @NonNull Team homeTeam, @NonNull Team awayTeam) {
        if (homeTeam.equals(awayTeam)) {
            throw new ScoreboardException(ErrorCodes.SAME_TEAM, homeTeam);
        }

        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startedAt = Instant.now();
        this.score = new Score(0, 0);
    }

    public ActiveMatch updateScore(@NonNull Score score) {
        this.score = score;
        return this;
    }

    public boolean conflicts(@NonNull ActiveMatch other) {
        return this.homeTeam.equals(other.homeTeam)
                || this.homeTeam.equals(other.awayTeam)
                || this.awayTeam.equals(other.homeTeam)
                || this.awayTeam.equals(other.awayTeam);
    }

    public ActiveMatch copy() {
        return new ActiveMatch(id, homeTeam, awayTeam, startedAt, score);
    }

}
