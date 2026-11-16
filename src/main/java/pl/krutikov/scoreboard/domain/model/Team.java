package pl.krutikov.scoreboard.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

@ToString
@EqualsAndHashCode
public final class Team {

    private final Country country;

    private Team(Country country) {
        this.country = country;
    }

    public static Team of(@NonNull String name) {
        return Country.find(name)
                .map(Team::new)
                .orElseThrow(() -> new ScoreboardException(ErrorCodes.UNSUPPORTED_TEAM, name));
    }

    public String name() {
        return country.getName();
    }

}
