package pl.krutikov.scoreboard.application.facade;

import lombok.RequiredArgsConstructor;
import pl.krutikov.scoreboard.api.Scoreboard;
import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.api.command.UpdateScoreCommand;
import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.api.query.GetHistoryQuery;
import pl.krutikov.scoreboard.application.service.ScoreboardCommandService;
import pl.krutikov.scoreboard.application.service.ScoreboardQueryService;

import java.util.List;

@RequiredArgsConstructor
public final class ScoreboardFacade implements Scoreboard {

    private final ScoreboardCommandService commandService;
    private final ScoreboardQueryService queryService;

    @Override
    public MatchView startMatch(StartMatchCommand command) {
        return commandService.startMatch(command);
    }

    @Override
    public MatchView updateScore(UpdateScoreCommand command) {
        return commandService.updateScore(command);
    }

    @Override
    public MatchView finishMatch(FinishMatchCommand command) {
        return commandService.finishMatch(command);
    }

    @Override
    public List<MatchView> getSummary() {
        return queryService.getSummary();
    }

    @Override
    public List<MatchView> getHistory(GetHistoryQuery query) {
        return queryService.getHistory(query);
    }

}
