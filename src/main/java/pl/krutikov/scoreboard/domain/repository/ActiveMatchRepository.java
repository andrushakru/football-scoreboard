package pl.krutikov.scoreboard.domain.repository;

import java.util.List;
import java.util.Optional;
import pl.krutikov.scoreboard.domain.model.ActiveMatch;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Score;

public interface ActiveMatchRepository {

    ActiveMatch add(ActiveMatch activeMatch);

    Optional<ActiveMatch> update(MatchId id, Score score);

    Optional<ActiveMatch> remove(MatchId id);

    List<ActiveMatch> findAll();

}
