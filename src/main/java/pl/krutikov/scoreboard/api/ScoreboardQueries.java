package pl.krutikov.scoreboard.api;

import pl.krutikov.scoreboard.api.model.MatchView;

import java.util.List;

public interface ScoreboardQueries {

    List<MatchView> getSummary();

}
