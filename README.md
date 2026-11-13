# Live Football World Cup Scoreboard

An in-memory Java library for starting football matches, assigning scores,
finishing matches, and retrieving an ordered summary of matches in progress.
Based on the Sportradar coding exercise.

Maven coordinates: `pl.krutikov:football-scoreboard:1.0-SNAPSHOT`.
Java 17 is the compilation baseline (`maven.compiler.release=17`).
The library has no runtime dependencies and requires no Spring, database, REST
server, or external cache.

## Usage

Use `Scoreboards.create()` to obtain the public `Scoreboard` interface. Each call
creates an independent scoreboard with its own repository and state. Consumers
need no application-service or repository configuration.

```java
import pl.krutikov.scoreboard.Scoreboards;
import pl.krutikov.scoreboard.api.Scoreboard;
import pl.krutikov.scoreboard.api.command.FinishMatchCommand;
import pl.krutikov.scoreboard.api.command.StartMatchCommand;
import pl.krutikov.scoreboard.api.command.UpdateScoreCommand;

Scoreboard scoreboard = Scoreboards.create();

var started = scoreboard.startMatch(StartMatchCommand.builder()
        .homeTeam("Mexico")
        .awayTeam("Canada")
        .build());

var updated = scoreboard.updateScore(UpdateScoreCommand.builder()
        .matchId(started.matchId())
        .homeScore(0)
        .awayScore(5)
        .build());

var summary = scoreboard.getSummary(); // Mexico 0–5 Canada

var finished = scoreboard.finishMatch(FinishMatchCommand.builder()
        .matchId(started.matchId())
        .build());
// scoreboard.getSummary() is now empty.
```

`Scoreboard` combines `ScoreboardCommands` and `ScoreboardQueries`. All three
commands return immutable `MatchView` records; summaries are unmodifiable lists
of those records. Public operations use UUIDs and strings, never domain entities.
Scores always use the original home/away orientation.

The inclusive score limit is `Constants.MAX_SCORE = 50`, allowing `0..50` per
team. There is no per-instance score configuration.

## Rules and design choices

The assignment specifies the four core operations and summary ordering. The
following validation and identity rules are implementation choices:

- Matches start at 0–0 with a fresh UUID. A team cannot play itself or participate
  in more than one active match, including reversed home/away pairings.
- Both scores must be in `0..50`, inclusive. `Score` validates every construction
  using `Constants.MAX_SCORE`, which is also supplied to the error message.
  Updates assign absolute scores; decreases are allowed for corrections.
  Totals use `int`.
- Finishing removes the match. The same teams can play again under a fresh UUID.
  Updating or finishing an absent match fails; finish is not idempotent.
- Team identity is backed by a `Teams` enum covering 249 ISO 3166-1 countries and
  territories. Lookup strips surrounding whitespace and uses uppercase keys;
  output uses canonical enum display names. Aliases and historical teams are not
  supported.
- Summary order is total goals descending, then `startedAt` descending. Each
  aggregate captures `Instant.now()` at construction. Clock changes and identical
  timestamps are accepted limitations; exact ties have unspecified order.

Business failures use the shared `exception.ScoreboardException`; inspect
`getErrorCode()` for `MATCH_NOT_FOUND`, `MATCH_ALREADY_EXISTS`,
`TEAM_ALREADY_PLAYING`, `SAME_TEAM`, `UNSUPPORTED_TEAM`, or `INVALID_SCORE`.
Messages use the templates in `ErrorCodes`. Required command fields use Lombok
`@NonNull`; ordinary null-argument failures remain `NullPointerException`.

## Architecture and concurrency

The design uses lightweight DDD and CQRS to separate public commands and queries,
application orchestration, and domain rules. The `Match` aggregate and value
objects enforce local invariants; the repository isolates storage concerns.
Reusable converters keep domain types out of the public API. Command buses,
event sourcing, converter registries, and DI frameworks would add complexity
without serving the current requirements.

In-memory storage keeps integration simple. Registrations are serialized to
prevent a team entering two active matches; updates and removal are atomic for
the same match ID. A linear conflict scan avoids maintaining a second index.
These choices favor straightforward correctness over additional infrastructure.

The service calls `Match.copy()` before sorting, keeping each match's score
stable for ordering and conversion while concurrent updates continue. Summary
reads are intentionally weakly consistent: globally atomic snapshots are not
guaranteed, and returned matches need not all have coexisted. No global read lock
is used. Command results may reflect a newer score observed after the repository
operation.

## Limitations

State is held only in memory. Team lookup uses the JVM default locale, so case
conversion can affect lookup results. Internal repository callers must respect
ownership of mutable matches; public API consumers receive only immutable views.

## Build and current scope

Use Maven 3.6.3+ and JDK 17 or newer:

```sh
mvn clean verify
# Compile production code without compiling or running tests:
mvn -Dmaven.test.skip=true clean compile
```

`mvn package` produces `target/football-scoreboard-1.0-SNAPSHOT.jar`. Lombok is
provided-scoped with explicit annotation processing; JUnit 5 and Mockito Jupiter
are test-scoped.

The tests cover core functionality, critical business rules, and important
concurrency behavior. The author deliberately avoids tests written merely to
increase coverage. DTOs and converters are not tested independently because they
contain no significant business logic; they are exercised through use cases.
The suite uses JUnit 5 and Mockito where appropriate and is not intended as
exhaustive verification of every concurrent interleaving.

The four core operations are implemented. The assignment's exactly one additional
operation is deferred and must be documented and introduced in a distinct Git
commit later. [AI.md](AI.md) records AI assistance and the key design prompts.
