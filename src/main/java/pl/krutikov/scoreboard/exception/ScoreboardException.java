package pl.krutikov.scoreboard.exception;

import lombok.Getter;

@Getter
public class ScoreboardException extends RuntimeException {

    private final ErrorCodes code;

    public ScoreboardException(ErrorCodes code, Object... arguments) {
        super(code.format(arguments));
        this.code = code;
    }

}
