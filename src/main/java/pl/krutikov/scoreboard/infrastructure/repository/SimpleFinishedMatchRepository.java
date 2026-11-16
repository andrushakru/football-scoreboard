package pl.krutikov.scoreboard.infrastructure.repository;

import pl.krutikov.scoreboard.constants.Constants;
import pl.krutikov.scoreboard.domain.model.FinishedMatch;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.repository.FinishedMatchRepository;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleFinishedMatchRepository implements FinishedMatchRepository {

    private final Map<MatchId, FinishedMatch> matches;
    private final Object lock = new Object();

    public SimpleFinishedMatchRepository() {
        this(Constants.MAX_FINISHED_MATCHES);
    }

    public SimpleFinishedMatchRepository(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Match history limit must be positive");
        }

        this.matches = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<MatchId, FinishedMatch> eldest) {
                return size() > limit;
            }
        };
    }

    @Override
    public FinishedMatch add(FinishedMatch match) {
        synchronized (lock) {
            if (matches.putIfAbsent(match.getId(), match) != null) {
                throw new ScoreboardException(ErrorCodes.MATCH_ALREADY_EXISTS, match.getId());
            }

            return match;
        }
    }

    @Override
    public List<FinishedMatch> findAll() {
        synchronized (lock) {
            return List.copyOf(matches.values());
        }
    }

}