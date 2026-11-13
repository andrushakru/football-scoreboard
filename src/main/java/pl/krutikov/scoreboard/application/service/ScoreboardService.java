package pl.krutikov.scoreboard.application.service;

import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.krutikov.scoreboard.api.Scoreboard;
import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.api.command.UpdateScoreCommand;
import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.application.converter.DomainToMatchViewConverter;
import pl.krutikov.scoreboard.application.converter.FinishMatchCommandToDomainConverter;
import pl.krutikov.scoreboard.application.converter.StartMatchCommandToDomainConverter;
import pl.krutikov.scoreboard.domain.model.Match;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Score;
import pl.krutikov.scoreboard.domain.repository.ActiveMatchRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public final class ScoreboardService implements Scoreboard {

    private final DomainToMatchViewConverter domainToViewConverter = new DomainToMatchViewConverter();
    private final StartMatchCommandToDomainConverter startCommandConverter = new StartMatchCommandToDomainConverter();
    private final FinishMatchCommandToDomainConverter finishCommandConverter = new FinishMatchCommandToDomainConverter();

    private final ActiveMatchRepository activeMatchRepository;

    @Override
    public MatchView startMatch(@NonNull StartMatchCommand command) {
        return Optional.of(command)
                .map(startCommandConverter::convert)
                .map(activeMatchRepository::add)
                .map(domainToViewConverter::convert)
                .orElseThrow();
    }

    @Override
    public MatchView updateScore(@NonNull UpdateScoreCommand command) {
        final var id = new MatchId(command.matchId());
        final var score = new Score(command.homeScore(), command.awayScore());

        return activeMatchRepository.update(id, score)
                .map(domainToViewConverter::convert)
                .orElseThrow(() -> new ScoreboardException(ErrorCodes.MATCH_NOT_FOUND, id));
    }

    @Override
    public MatchView finishMatch(@NonNull FinishMatchCommand command) {
        return Optional.of(command)
                .map(finishCommandConverter::convert)
                .flatMap(activeMatchRepository::remove)
                .map(domainToViewConverter::convert)
                .orElseThrow(() -> new ScoreboardException(ErrorCodes.MATCH_NOT_FOUND, command.matchId()));
    }

    @Override
    public List<MatchView> getSummary() {
        return activeMatchRepository.findAll()
                .stream()
                .map(Match::copy)
                .sorted(summaryComparator())
                .map(domainToViewConverter::convert)
                .toList();
    }

    private Comparator<Match> summaryComparator() {
        return Comparator.comparingInt((Match match) -> match.getScore().total())
                .reversed()
                .thenComparing(Match::getStartedAt, Comparator.reverseOrder());
    }

}
