package pl.krutikov.scoreboard.application.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.krutikov.scoreboard.api.ScoreboardCommands;
import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.api.command.UpdateScoreCommand;
import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.application.converter.ActiveMatchToViewConverter;
import pl.krutikov.scoreboard.application.converter.FinishMatchCommandToDomainConverter;
import pl.krutikov.scoreboard.application.converter.FinishedMatchToViewConverter;
import pl.krutikov.scoreboard.application.converter.StartMatchCommandToDomainConverter;
import pl.krutikov.scoreboard.domain.model.FinishedMatch;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Score;
import pl.krutikov.scoreboard.domain.repository.ActiveMatchRepository;
import pl.krutikov.scoreboard.domain.repository.FinishedMatchRepository;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.Optional;

@RequiredArgsConstructor
public final class ScoreboardCommandService implements ScoreboardCommands {

    private final StartMatchCommandToDomainConverter startCommandConverter = new StartMatchCommandToDomainConverter();
    private final FinishMatchCommandToDomainConverter finishCommandConverter = new FinishMatchCommandToDomainConverter();
    private final ActiveMatchToViewConverter activeMatchToViewConverter = new ActiveMatchToViewConverter();
    private final FinishedMatchToViewConverter finishedMatchToViewConverter = new FinishedMatchToViewConverter();

    private final ActiveMatchRepository activeMatchRepository;
    private final FinishedMatchRepository finishedMatchRepository;

    @Override
    public MatchView startMatch(@NonNull StartMatchCommand command) {
        return Optional.of(command)
                .map(startCommandConverter::convert)
                .map(activeMatchRepository::add)
                .map(activeMatchToViewConverter::convert)
                .orElseThrow();
    }

    @Override
    public MatchView updateScore(@NonNull UpdateScoreCommand command) {
        final var id = new MatchId(command.matchId());
        final var score = new Score(command.homeScore(), command.awayScore());

        return activeMatchRepository.update(id, score)
                .map(activeMatchToViewConverter::convert)
                .orElseThrow(() -> new ScoreboardException(ErrorCodes.MATCH_NOT_FOUND, id));
    }

    @Override
    public MatchView finishMatch(@NonNull FinishMatchCommand command) {
        return Optional.of(command)
                .map(finishCommandConverter::convert)
                .flatMap(activeMatchRepository::remove)
                .map(FinishedMatch::from)
                .map(finishedMatchRepository::add)
                .map(finishedMatchToViewConverter::convert)
                .orElseThrow(() -> new ScoreboardException(ErrorCodes.MATCH_NOT_FOUND, command.matchId()));
    }

}
