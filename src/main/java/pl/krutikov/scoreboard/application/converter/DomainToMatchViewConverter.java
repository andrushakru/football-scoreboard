package pl.krutikov.scoreboard.application.converter;

import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.domain.model.Match;
import pl.krutikov.scoreboard.domain.model.Score;

public final class DomainToMatchViewConverter implements Converter<Match, MatchView> {

    @Override
    public MatchView convert(Match source) {
        final var score = source.getScore();

        return MatchView.builder()
                .matchId(source.getId().value())
                .homeTeam(source.getHomeTeam().getTeam().getName())
                .awayTeam(source.getAwayTeam().getTeam().getName())
                .homeScore(score.home())
                .awayScore(score.away())
                .startedAt(source.getStartedAt())
                .build();
    }

}
