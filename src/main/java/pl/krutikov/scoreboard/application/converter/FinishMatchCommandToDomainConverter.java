package pl.krutikov.scoreboard.application.converter;

import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.domain.model.MatchId;

public class FinishMatchCommandToDomainConverter implements Converter<FinishMatchCommand, MatchId> {

    @Override
    public MatchId convert(FinishMatchCommand source) {
        return new MatchId(source.matchId());
    }

}
