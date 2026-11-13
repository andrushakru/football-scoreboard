package pl.krutikov.scoreboard.api.command;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record StartMatchCommand(@NonNull
                                String homeTeam,
                                @NonNull
                                String awayTeam) {
}
