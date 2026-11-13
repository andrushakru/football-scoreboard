package pl.krutikov.scoreboard.api;

import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.api.command.UpdateScoreCommand;
import pl.krutikov.scoreboard.api.model.MatchView;

public interface ScoreboardCommands {

    MatchView startMatch(StartMatchCommand command);

    MatchView updateScore(UpdateScoreCommand command);

    MatchView finishMatch(FinishMatchCommand command);

}
