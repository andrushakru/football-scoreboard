package pl.krutikov.scoreboard;

import pl.krutikov.scoreboard.api.Scoreboard;
import pl.krutikov.scoreboard.constants.Constants;
import pl.krutikov.scoreboard.application.facade.ScoreboardFacade;
import pl.krutikov.scoreboard.application.service.ScoreboardCommandService;
import pl.krutikov.scoreboard.application.service.ScoreboardQueryService;
import pl.krutikov.scoreboard.infrastructure.repository.SimpleActiveMatchRepository;
import pl.krutikov.scoreboard.infrastructure.repository.SimpleFinishedMatchRepository;

public final class Scoreboards {

    private Scoreboards() {
    }

    public static Scoreboard create() {
        return create(Constants.MAX_FINISHED_MATCHES);
    }

    public static Scoreboard create(int maxFinishedMatches) {
        final var activeMatchRepository = new SimpleActiveMatchRepository();
        final var finishedMatchRepository = new SimpleFinishedMatchRepository(maxFinishedMatches);
        final var commandService = new ScoreboardCommandService(activeMatchRepository, finishedMatchRepository);
        final var queryService = new ScoreboardQueryService(activeMatchRepository, finishedMatchRepository);

        return new ScoreboardFacade(commandService, queryService);
    }

}
