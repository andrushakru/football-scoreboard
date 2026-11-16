package pl.krutikov.scoreboard.api;

import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.api.query.GetHistoryQuery;

import java.util.List;

public interface ScoreboardQueries {

    List<MatchView> getSummary();

    List<MatchView> getHistory(GetHistoryQuery query);

}
