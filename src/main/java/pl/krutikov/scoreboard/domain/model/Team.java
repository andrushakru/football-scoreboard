package pl.krutikov.scoreboard.domain.model;

import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@ToString
@Getter
@EqualsAndHashCode
public final class Team {

    private final Teams team;

    private Team(Teams team) {
        this.team = team;
    }

    public static Team of(@NonNull String name) {
        return Teams.find(name)
                .map(Team::new)
                .orElseThrow(() -> new ScoreboardException(ErrorCodes.UNSUPPORTED_TEAM, name));
    }

}
