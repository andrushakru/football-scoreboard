package pl.krutikov.scoreboard.infrastructure.repository;

import pl.krutikov.scoreboard.domain.model.ActiveMatch;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Score;
import pl.krutikov.scoreboard.domain.repository.ActiveMatchRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleActiveMatchRepository implements ActiveMatchRepository {

    private final Map<MatchId, ActiveMatch> matches = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @Override
    public ActiveMatch add(ActiveMatch match) {
        synchronized (lock) {
            if (matches.containsKey(match.getId())) {
                throw new ScoreboardException(ErrorCodes.MATCH_ALREADY_EXISTS, match.getId());
            }
            if (matches.values().stream().anyMatch(match::conflicts)) {
                throw new ScoreboardException(ErrorCodes.TEAM_ALREADY_PLAYING, match.getHomeTeam(), match.getAwayTeam());
            }

            matches.put(match.getId(), match);

            return match;
        }
    }

    @Override
    public Optional<ActiveMatch> update(MatchId id, Score score) {
        return Optional.ofNullable(
                matches.computeIfPresent(id, (key, match) -> match.updateScore(score))
        );
    }

    @Override
    public Optional<ActiveMatch> remove(MatchId id) {
        return Optional.ofNullable(matches.remove(id));
    }

    @Override
    public List<ActiveMatch> findAll() {
        return List.copyOf(matches.values());
    }

}
