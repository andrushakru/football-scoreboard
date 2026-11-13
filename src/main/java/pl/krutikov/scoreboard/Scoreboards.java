package pl.krutikov.scoreboard;

import pl.krutikov.scoreboard.api.Scoreboard;
import pl.krutikov.scoreboard.application.service.ScoreboardService;
import pl.krutikov.scoreboard.infrastructure.repository.SimpleActiveMatchRepository;

public final class Scoreboards {

    private Scoreboards() {
    }

    public static Scoreboard create() {
        return new ScoreboardService(new SimpleActiveMatchRepository());
    }

}
