package pl.krutikov.scoreboard.application.converter;

import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.domain.model.ActiveMatch;

public final class ActiveMatchToViewConverter implements Converter<ActiveMatch, MatchView> {

    @Override
    public MatchView convert(ActiveMatch source) {
        final var score = source.getScore();

        return MatchView.builder()
                .matchId(source.getId().value())
                .homeTeam(source.getHomeTeam().getCountry().getName())
                .awayTeam(source.getAwayTeam().getCountry().getName())
                .homeScore(score.home())
                .awayScore(score.away())
                .startedAt(source.getStartedAt())
                .build();
    }

}
