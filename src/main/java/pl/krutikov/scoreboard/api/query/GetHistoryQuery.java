package pl.krutikov.scoreboard.api.query;

import lombok.Builder;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

@Builder
public record GetHistoryQuery(int limit) {

    public GetHistoryQuery {
        if (limit <= 0) {
            throw new ScoreboardException(ErrorCodes.INVALID_HISTORY_LIMIT, limit);
        }
    }

}
