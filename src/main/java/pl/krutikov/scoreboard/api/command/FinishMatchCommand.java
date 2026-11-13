package pl.krutikov.scoreboard.api.command;

import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record FinishMatchCommand(@NonNull
                                 UUID matchId) {
}
