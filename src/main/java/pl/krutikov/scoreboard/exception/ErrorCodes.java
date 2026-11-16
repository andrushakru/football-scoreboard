package pl.krutikov.scoreboard.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCodes {

    MATCH_NOT_FOUND("Match not found: %s"),
    MATCH_ALREADY_EXISTS("Match id already registered: %s"),
    TEAM_ALREADY_PLAYING("At least one team already has an active match: %s, %s"),
    SAME_TEAM("A team cannot play against itself: %s"),
    UNSUPPORTED_TEAM("Unsupported team: %s"),
    INVALID_HISTORY_LIMIT("History limit must be greater than 0: limit=%s"),
    INVALID_SCORE("Scores must be between 0 and %s: home=%s, away=%s");

    private final String template;

    public String format(Object... arguments) {
        return template.formatted(arguments);
    }

}
