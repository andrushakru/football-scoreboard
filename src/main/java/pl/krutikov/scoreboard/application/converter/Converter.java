package pl.krutikov.scoreboard.application.converter;

import lombok.NonNull;

import java.util.List;

public interface Converter<S, T> {

    T convert(S source);

    default List<T> convert(@NonNull List<? extends S> sources) {
        return sources.stream()
                .map(this::convert)
                .toList();
    }

}
