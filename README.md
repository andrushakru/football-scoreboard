# Live Football World Cup Scoreboard

An in-memory Java 17 library implementing the Sportradar coding exercise. It supports starting matches, updating scores, finishing matches, and retrieving an ordered summary of active matches.

The project also implements one additional operation: **finished match history**.

## Usage

Create an independent scoreboard with the default history capacity of 200 matches, or specify a custom capacity:

```java
Scoreboard scoreboard = Scoreboards.create();
Scoreboard custom = Scoreboards.create(20);
```

Example:

```java
var match = scoreboard.startMatch(
        StartMatchCommand.builder()
                .homeTeam("Mexico")
                .awayTeam("Canada")
                .build()
);

scoreboard.updateScore(
        UpdateScoreCommand.builder()
                .matchId(match.matchId())
                .homeScore(0)
                .awayScore(5)
                .build()
);

var summary = scoreboard.getSummary();

scoreboard.finishMatch(
        FinishMatchCommand.builder()
                .matchId(match.matchId())
                .build()
);

var history = scoreboard.getHistory(
        GetHistoryQuery.builder()
                .limit(10)
                .build()
);
```

`getSummary()` returns active matches ordered by total score descending, then by start time (most recent first).

Commands return immutable `MatchView` records, and queries return unmodifiable lists.

## Additional operation: Match history

I chose **finished match history** instead of a simple lookup by match ID because it introduces more interesting design decisions while providing useful functionality.

Finished matches disappear from the live scoreboard, but their final results may still be useful. `getHistory()` makes them available without mixing completed matches into the active summary.

History is ordered by completion time, newest first, and has a configurable storage capacity.

**Why limit history storage?**

The active scoreboard is naturally bounded: with 249 supported countries and each team allowed in only one active match, at most **124 matches** can be active simultaneously. No additional eviction mechanism is needed to keep active storage bounded.

Finished matches have no such natural limit. Without eviction, history would continue growing as more matches are completed.

For this reason, history uses a bounded collection with a default capacity of 200 matches. When full, the oldest inserted result is removed. The capacity can be configured using `Scoreboards.create(int)`.

The `GetHistoryQuery` limit controls how many retained matches are returned, independently of storage capacity.

## Business rules

- Matches start at 0–0 with a unique UUID.
- A team cannot play itself or participate in multiple active matches.
- Scores range from 0 to 50, inclusive. Updates replace the current score.
- Finishing removes a match from the active scoreboard, saves its final result, and releases both teams.
- Team names use an ISO 3166-1-based `Country` catalog, which does not cover every FIFA national team.
- Business errors from scoreboard operations throw `ScoreboardException` with the appropriate error code. Invalid factory history capacity is a configuration error and throws `IllegalArgumentException`.

## Architecture and concurrency

The implementation uses lightweight DDD and CQRS. `ScoreboardFacade` delegates commands and queries to separate services sharing the same repositories.

Active matches are mutable aggregates stored in a `ConcurrentHashMap`. Finished matches are immutable snapshots stored in a bounded `LinkedHashMap`.

**Reads do not require global synchronization.**

Starting a match is synchronized to prevent concurrent registrations involving the same team. Score updates and removals use atomic operations for individual match IDs.

`getSummary()` copies each active match before sorting, keeping its score stable within the result. The overall summary is intentionally **weakly consistent**, not a globally atomic snapshot.

`getHistory()` uses a short lock to copy the finished-match collection. Sorting and conversion happen outside the lock.

This is a deliberate trade-off: we favor simple concurrency and non-blocking active reads over globally consistent snapshots. Stronger synchronization could be introduced if required.

Moving a match from active storage to history is not transactional. Concurrent readers may briefly observe it in neither collection.

## Build and tests

Requires JDK 17+ and Maven.

```bash
mvn clean verify
```

The tests cover core behavior, business rules, ordering, repository limits, and concurrent match registration.

The library has no database, Spring dependency, or external runtime infrastructure. All state is kept in memory.

See `AI.md` for details of AI assistance during development.