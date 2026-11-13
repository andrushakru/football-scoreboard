package pl.krutikov.scoreboard.domain.model;

import lombok.NonNull;

import java.util.UUID;

public record MatchId(@NonNull UUID value) {
}
