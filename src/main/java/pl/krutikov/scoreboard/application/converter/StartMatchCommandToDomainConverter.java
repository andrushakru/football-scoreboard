package pl.krutikov.scoreboard.application.converter;

import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.domain.model.ActiveMatch;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Team;

import java.util.UUID;

public final class StartMatchCommandToDomainConverter implements Converter<StartMatchCommand, ActiveMatch> {

    @Override
    public ActiveMatch convert(StartMatchCommand source) {
        final var id = new MatchId(UUID.randomUUID());
        final var home = Team.of(source.homeTeam());
        final var away = Team.of(source.awayTeam());

        return new ActiveMatch(id, home, away);
    }

}
