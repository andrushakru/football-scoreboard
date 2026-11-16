package pl.krutikov.scoreboard.infrastructure.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.krutikov.scoreboard.domain.model.ActiveMatch;
import pl.krutikov.scoreboard.domain.model.FinishedMatch;
import pl.krutikov.scoreboard.domain.model.MatchId;
import pl.krutikov.scoreboard.domain.model.Team;
import pl.krutikov.scoreboard.exception.ErrorCodes;
import pl.krutikov.scoreboard.exception.ScoreboardException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SimpleFinishedMatchRepositoryTest {

    @InjectMocks
    private SimpleFinishedMatchRepository repository;

    @Test
    void add_rejectsDuplicates() {
        // Given
        final var id = new MatchId(UUID.randomUUID());
        final var existing = FinishedMatch.from(new ActiveMatch(id, Team.of("Mexico"), Team.of("Canada")));
        final var duplicate = FinishedMatch.from(new ActiveMatch(id, Team.of("Brazil"), Team.of("France")));

        repository.add(existing);

        // When
        final var exception = assertThrows(ScoreboardException.class, () -> repository.add(duplicate));

        // Then
        assertAll(
                () -> assertEquals(ErrorCodes.MATCH_ALREADY_EXISTS, exception.getCode()),
                () -> assertEquals(List.of(existing), repository.findAll())
        );
    }

    @Test
    void add_removesOldestMatchWhenLimitReached() {
        // Given
        final var repository = new SimpleFinishedMatchRepository(2);

        final var first = FinishedMatch.from(new ActiveMatch(
                new MatchId(UUID.randomUUID()), Team.of("Mexico"), Team.of("Canada")));
        final var second = FinishedMatch.from(new ActiveMatch(
                new MatchId(UUID.randomUUID()), Team.of("Brazil"), Team.of("France")));
        final var third = FinishedMatch.from(new ActiveMatch(
                new MatchId(UUID.randomUUID()), Team.of("Spain"), Team.of("Germany")));

        // When
        repository.add(first);
        repository.add(second);
        repository.add(third);

        // Then
        assertEquals(List.of(second, third), repository.findAll());
    }

}