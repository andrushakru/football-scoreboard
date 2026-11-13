package pl.krutikov.scoreboard.api.command;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

@Builder
public record UpdateScoreCommand(@NonNull
                                 UUID matchId,
                                 int homeScore,
                                 int awayScore) {
}
