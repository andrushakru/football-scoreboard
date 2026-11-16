package pl.krutikov.scoreboard.application.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.krutikov.scoreboard.api.ScoreboardQueries;
import pl.krutikov.scoreboard.api.model.MatchView;
import pl.krutikov.scoreboard.api.query.GetHistoryQuery;
import pl.krutikov.scoreboard.application.converter.ActiveMatchToViewConverter;
import pl.krutikov.scoreboard.application.converter.FinishedMatchToViewConverter;
import pl.krutikov.scoreboard.domain.model.ActiveMatch;
import pl.krutikov.scoreboard.domain.model.FinishedMatch;
import pl.krutikov.scoreboard.domain.repository.ActiveMatchRepository;
import pl.krutikov.scoreboard.domain.repository.FinishedMatchRepository;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public final class ScoreboardQueryService implements ScoreboardQueries {

    private final ActiveMatchToViewConverter activeMatchToViewConverter = new ActiveMatchToViewConverter();
    private final FinishedMatchToViewConverter finishedMatchToViewConverter = new FinishedMatchToViewConverter();

    private final ActiveMatchRepository activeMatchRepository;
    private final FinishedMatchRepository finishedMatchRepository;

    @Override
    public List<MatchView> getSummary() {
        return activeMatchRepository.findAll()
                .stream()
                .map(ActiveMatch::copy)
                .sorted(summaryComparator())
                .map(activeMatchToViewConverter::convert)
                .toList();
    }

    @Override
    public List<MatchView> getHistory(@NonNull GetHistoryQuery query) {
        return finishedMatchRepository.findAll()
                .stream()
                .sorted(historyComparator())
                .limit(query.limit())
                .map(finishedMatchToViewConverter::convert)
                .toList();
    }

    private Comparator<FinishedMatch> historyComparator() {
        return Comparator.comparing(FinishedMatch::getFinishedAt).reversed();
    }

    private Comparator<ActiveMatch> summaryComparator() {
        return Comparator.comparingInt((ActiveMatch activeMatch) -> activeMatch.getScore().total())
                .reversed()
                .thenComparing(ActiveMatch::getStartedAt, Comparator.reverseOrder());
    }

}
