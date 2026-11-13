package pl.krutikov.scoreboard.domain.repository;

import java.util.List;
import java.util.Optional;
import pl.krutikov.scoreboard.domain.model.Match;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Score;

public interface ActiveMatchRepository {

    Match add(Match match);

    Optional<Match> update(MatchId id, Score score);

    Optional<Match> remove(MatchId id);

    List<Match> findAll();

}
