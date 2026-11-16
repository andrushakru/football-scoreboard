package pl.krutikov.scoreboard.domain.repository;

import pl.krutikov.scoreboard.domain.model.FinishedMatch;

import java.util.List;

public interface FinishedMatchRepository {

    FinishedMatch add(FinishedMatch match);

    List<FinishedMatch> findAll();

}
