package pl.krutikov.scoreboard.domain.model;

import pl.krutikov.scoreboard.constants.Constants;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

public record Score(int home, int away) {

    public Score {
        if (home < 0 || home > Constants.MAX_SCORE || away < 0 || away > Constants.MAX_SCORE) {
            throw new ScoreboardException(ErrorCodes.INVALID_SCORE, Constants.MAX_SCORE, home, away);
        }
    }

    public int total() {
        return home + away;
    }

}
