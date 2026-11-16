# AI assistance and development record

## Purpose and evidence

This document records how OpenAI Codex helped develop the Football World Cup
Scoreboard and how the author reviewed and changed that work. It explains the
requirements, main prompts, design decisions, rejected approaches, and remaining
limitations. It is a development record, not a complete conversation transcript.

The record uses several kinds of evidence:

- **Original assignment:** the local `docs/ODDS and Data - JAVA Coding Task.pdf`,
  read again for this update. The `docs` directory is ignored by Git, so the PDF
  may not be present in a submitted checkout.
- **Current artifacts:** production code, tests, `pom.xml`, and the author's
  shortened [README.md](README.md). These establish current behavior.
- **Available conversation:** direct instructions and AI responses from the
  country rename through history development, CQRS separation, and test cleanup.
- **Earlier AI record:** the previous version of this file describes initial
  setup and core development. Those details are retained as earlier recorded
  summaries; the full original conversations are not available here.
- **Git history:** `d2825d0` is the initial commit and `ab2fa01` is
  `feature_1: implemented core scoreboard functionality`. At this review, later
  history and CQRS changes remain in the working tree or index. Git does not
  provide a separate commit for every conversation step.

Prompts below are shortened summaries unless marked as a verified excerpt.
Earlier recorded prompts are not presented as newly verified exact quotations.
Code explains what exists; it does not by itself prove the author's motivation
or identify who wrote each line.

## Assignment and project context

The Sportradar exercise asks for a simple Java library in a Maven project. It
requires four core operations: start a match, update its score, finish a match,
and get a summary of matches in progress. The summary must sort by total goals
in descending order, then by most recent start time for equal totals.

The assignment's example contains Mexico–Canada, Spain–Brazil, Germany–France,
Uruguay–Italy, and Argentina–Australia. Its expected order is Uruguay, Spain,
Mexico, Argentina, Germany. The query-service test uses this example as a
concrete reference for ordering.

The exercise also requires exactly one chosen additional operation, documented
in README and introduced in a distinct Git commit. It explicitly requires AI
assistance with design, reasoning, and code generation. `AI.md` must explain AI
use, prompt history, other context, and artifacts that guided implementation.

The assignment leaves many choices open. UUID identity, supported countries,
score bounds, team conflicts, concurrency guarantees, and history capacity are
project decisions. They should not be mistaken for explicit assignment rules.
The library targets Java 17 and keeps state in memory. It has no database,
Spring application, REST server, message broker, or external cache.

## AI contribution and author ownership

Codex generated initial code, proposed designs, discussed alternatives, reviewed
concurrency, updated code after instructions, generated tests, ran Maven checks,
and helped with documentation. The author selected the direction and made the
final architectural and implementation decisions.

AI output often needed substantial revision. The earlier record reports overly
complex code, weak structure, and insufficient attention to domain rules and API
contracts. Tests were a repeated problem: excessive mocking, boilerplate,
redundant assertions, and complicated concurrency setup led to direct manual
rewrites and further simplification requests.

The author's work was not limited to accepting generated patches. The author
changed domain classes, repositories, converters, services, public models, and
tests, both personally and through specific instructions. Later prompts
explicitly told Codex to preserve working tests and the author's rewritten
README. Those edits are part of the final design, not AI-generated work that
can be attributed entirely to Codex.

The evidence supports mixed authorship and repeated review. It does not support
a line-by-line attribution of the whole repository. No Git commits were created
by Codex in the available development record.

## Development stages and representative prompts

### 1. Setup and core implementation

**Earlier recorded summary:** initialize a Java library with Maven, then adjust
coordinates and packages. Codex supplied scaffolding, dependency configuration,
and an initial smoke test. The current artifact is
`pl.krutikov:football-scoreboard:1.0-SNAPSHOT` with compiler release 17.

**Earlier recorded summary:** first study the assignment and the author's
proposed lightweight DDD/CQRS structure. The author selected the layers and
constrained the abstractions before implementation.

**Earlier recorded summary:** implement the domain model, command/query API,
application service, and repository abstraction. AI generated an initial version;
the author then reshaped construction, identity, repository contracts, converters,
and public access. The core commit provides a checkpoint, but does not preserve
every intermediate proposal.

**Earlier recorded summary:** review concurrency and simplify the solution.
The earlier record attributes synchronized registration, atomic map operations,
volatile immutable scores, copying before sorting, and weakly consistent reads
to the author's selected approach. It also records the decision to use shared
`ScoreboardException` and `ErrorCodes`. Configurable score bounds were explored
and removed; the current bound remains fixed at 0–50.

### 2. Clearer country naming

**Verified excerpt:** "rename public enum Teams to Country or Countries".
The author found `Teams` inside `Team` confusing. Codex renamed the enum to
`Country`, changed the field to `country`, and updated getter usages and
conversion. The country catalog and lookup behavior remained the same.

### 3. Choosing and implementing finished history

**Prompt summary:** keep active storage separate; add a finished repository,
retain up to 200 results in a `LinkedHashMap`, store immutable final data with
`finishedAt`, and add a newest-first history query. Keep the design simple,
thread-safe, and free of events or message buses. Consider history-save failure
after active removal. Initially, tests and documentation were excluded.

Codex first added `FinishedMatch`, a separate view, a finished repository, and a
history query. To avoid losing a removed match if saving failed, it also added
an active-repository removal callback. That callback saved history inside the
atomic removal operation and used an `AtomicReference` to return the removed
match. This was an AI implementation choice responding to the failure concern.
It is not the current approach.

### 4. Removing history complexity

**Prompt summary:** return storage data without reversing it; sort in the
service; replace the finished record with an immutable final class; use
`FinishedMatch.from()`; restore simple removal and accept two nontransactional
in-memory operations.

The author explicitly replaced the callback design. `Consumer` and
`AtomicReference` were removed. The command flow now removes the active match,
creates the finished snapshot, saves it, and converts the result. Sorting moved
to a private service comparator. A separate domain-to-domain converter was
rejected in favor of the factory on `FinishedMatch`.

**Prompt summary:** use one `MatchView` for both active and finished matches,
while keeping `ActiveMatchToViewConverter` and `FinishedMatchToViewConverter`.
The author had already renamed `Match` to `ActiveMatch`. Codex fixed remaining
references and removed dependence on the duplicate finished DTO.

**Prompt summary:** make `finishMatch()` one Optional chain and return the saved
entity from repository `add()`. Use a `Map` field backed by `LinkedHashMap` and
`removeEldestEntry()` instead of manual iterator removal. Move the default limit
to `Constants`. A later request replaced synchronized methods with a private
lock and required duplicate-ID rejection. Current insertion uses
`putIfAbsent()` under that lock.

### 5. Separating query limit from storage capacity

**Prompt summary:** rename `getRecentFinishedMatches()` to `getHistory()` and
its comparator to `historyComparator()`; introduce a query with a limit.
The initial requested shape included `HistoryCriteria`, a converter, an upper
limit tied to storage, and an Optional chain.

**Verified excerpt:** "The storage limit and query limit are different things."
After review, the author removed the criteria and converter. Validation moved
to `GetHistoryQuery`: the limit must be positive, with no upper check against
storage capacity. `getHistory()` became a direct stream pipeline. This is a
verified example of an earlier requested abstraction being reconsidered and
removed, rather than a claim that every simplification corrected an AI proposal.

### 6. Making command and query services explicit

**Prompt summary:** split `ScoreboardService` into command and query services;
move the public implementation to `application.facade.ScoreboardFacade`; share
repository instances through `Scoreboards.create()`. Keep converters where they
are and add no frameworks or new interfaces.

The write methods moved to `ScoreboardCommandService`; reads and comparators
moved to `ScoreboardQueryService`. The facade only delegates.

The author later requested interface dependencies in the facade, then changed
field naming, then asked to restore concrete service classes. The current
facade therefore depends on `ScoreboardCommandService commandService` and
`ScoreboardQueryService queryService`. This is the latest verified preference;
the record does not invent a broader reason for that reversal.

### 7. Reviewing and reducing tests

**Verified excerpt:** "Do not rewrite, simplify, or refactor tests that already
work." The author requested only necessary rename/API fixes and meaningful
history coverage, with JUnit 5, `@MethodSource`, `final var`, and Given/When/Then
comments.

Codex added history, capacity, duplicate, sorting, and concurrency tests. The
author subsequently refactored the suite personally and requested further
cleanup. An immutability test was removed as obvious behavior. A deterministic
completion-before-start test was retained. Public API history tests moved from
`ScoreboardFacadeTest` to `ScoreboardTest`; the two query sorting tests stayed
separate. Extra match setup and redundant cases were removed.

### 8. Configurable capacity and documentation

**Prompt summary:** add `Scoreboards.create(int maxFinishedMatches)`, preserve
the default 200, validate positive capacity, update README, and preserve existing
tests. The repository already accepted a constructor capacity when this request
was implemented. The factory overload passed the value through that validation.

Codex initially added a separate parameterized factory test class. The author
rejected it as too complicated and requested one positive test as the second
method of `ScoreboardTest`: finish three matches with capacity two and check that
only the last two remain. The current suite follows that direction.

The author then shortened README personally. This documentation update preserves
that file and expands only `AI.md` after reviewing the assignment and project.

## Current design and its reasons

### Public access and lightweight CQRS

`Scoreboard` combines the existing command and query interfaces. Both factory
methods return that interface and create independent storage. Within one
scoreboard, the two services share the same active and finished repository
instances. Consumers do not need to assemble services or repositories.

The author requested separate read and write services to make CQRS clearer.
This separation is local Java organization, not separate deployed systems or an
asynchronous projection. Command completion inserts history directly. No event
bus, event sourcing, strategy interface, converter registry, or dependency
injection framework was introduced. These additions were excluded or considered
unnecessary for the current scope.

### Domain state and public data

`ActiveMatch` keeps stable identity, teams, and start time, with a replaceable
score. `Score` is immutable and validates every construction. A volatile score
reference allows readers to observe replacement without exposing a partly
updated home/away pair. Updates are absolute assignments; decreases allow score
corrections. Totals use `int`, and each team's score is limited to 0–50.

`FinishedMatch` is a final class with final fields, getters, and no update
methods. Its factory captures the active data and current completion time.
It checks required values, different teams, and completion not before start.
The author explicitly requested this class shape and factory. Immutable finished
data represents a result that is no longer updated.

One public `MatchView` avoids duplicate DTOs. Its `finishedAt` is null for active
results and populated for finished results. Both domain-to-view converters stay
separate because their source types differ. Required command fields use Lombok
null checks. Business errors expose `ScoreboardException.getCode()`; invalid
history capacity and impossible completion chronology use
`IllegalArgumentException` in the current implementation.

`Team` wraps a `Country` entry and compares by that identity. Lookup strips
surrounding whitespace and uppercases names. The catalog contains 249 ISO-based
countries and territories. As the author's README notes, this is not a full
catalog of FIFA national teams. Aliases and historical teams are unsupported.
Case conversion uses the default JVM locale, an accepted lookup limitation
recorded during earlier development.

### Why history was the additional operation

The author's current README gives the reason directly: history provides useful
finished results and introduces more interesting design choices than a simple
lookup by match ID. A finished result disappears from the live summary but can
still be useful to callers. Separate storage preserves that distinction and
supports the requested small step toward clearer CQRS.

Active storage has a natural bound under current rules. There are 249 supported
team identities, two different teams per match, and no team may participate in
two active matches. Therefore, at most 124 matches can be active. This follows
from the current catalog and conflict rule; it is not a fixed storage capacity
or a general statement about every possible football library.

Finished results can grow without bound because released teams may play again
under new UUIDs. History therefore needs eviction. `create()` retains up to 200
finished matches; `create(int)` accepts any positive capacity. The constant
`MAX_FINISHED_MATCHES` is the default, not an upper bound on custom capacity.

History uses insertion-order `LinkedHashMap` storage behind `Map`.
`removeEldestEntry()` removes the oldest inserted result when capacity is
exceeded. Duplicate retained IDs are rejected instead of overwriting results.
This is bounded recent storage, not a permanent archive or permanent duplicate-ID
registry after entries have been evicted.

Storage order and query order are different responsibilities. The repository
copies its values without sorting. The query service sorts by `finishedAt`
descending, applies the positive query limit, and converts to `MatchView`.
A query limit above capacity is valid and returns only available results.
Concurrent finishes can insert in an order different from timestamp order;
eviction still follows insertion order.

## Concurrency and accepted trade-offs

Active storage uses `ConcurrentHashMap`. Registration holds a private lock
across duplicate detection, team-conflict scanning, and insertion. A thread-safe
map alone would not make that full check-and-add sequence safe. The linear scan
avoids a second team index and the extra consistency work such an index needs.

Updates use `computeIfPresent()` and removal uses the map's atomic removal.
For public repository-mediated operations on one ID, an update either completes
before removal or finds no active match afterward. Finishing is not idempotent;
updating or finishing an absent ID reports `MATCH_NOT_FOUND`.

Summary reads do not take a global lock. They collect active values, copy each
match, and sort those copies. Each copied score remains stable during ordering
and conversion. The whole result is weakly consistent: matches may reflect
different moments, and returned matches need not all have existed together.
Start/update return conversion can also observe a newer concurrent score.
Internal callers must respect ownership of mutable active entities.

Finished storage uses one private lock for insertion, duplicate checking,
eviction, and copying. History sorting and conversion happen after the copy,
outside the lock. Thus, avoiding global read synchronization does not mean that
history has no locking; it means no single lock protects every operation across
both stores.

The author explicitly accepted removal and history insertion as separate,
nontransactional operations. A reader may briefly see a match in neither store.
If snapshot creation or saving fails after removal, the match is not restored.
This includes a backward clock movement that makes `finishedAt` precede
`startedAt`. The earlier callback approach reduced that failure risk but added
coordination the author chose to remove. The current code makes no rollback,
retry, durable-storage, or cross-repository atomicity guarantee.

Times come from `Instant.now()`. Equal timestamps have no explicit tie-breaker;
clock changes can affect chronological ordering. An injected clock, sequence
number, stronger snapshot lock, or transaction could address different future
requirements, but none was added. These are limits of the chosen scope, not
promised features. All state is lost when its scoreboard instance is discarded.

## Testing decisions and current evidence

The author favors meaningful behavior checks over coverage counts. Public API
command tests in `ScoreboardTest` exercise factory wiring, facade delegation,
services, converters, and storage together. Separate mocked command-service
copies of the same scenarios would add repetition. The current tests cover
independent scoreboards, configured capacity, initialization, team conflicts,
unsupported teams, score replacement, invalid scores, finishing, and history.

`ScoreboardQueryServiceTest` keeps two focused sorting tests. Mock repositories
provide deliberately chosen orderings, and static `Instant` mocking supplies
fixed times without sleeps. The summary test follows the assignment example.
The history test makes completion order differ from insertion order and checks
that sorting happens before limiting.

Domain tests cover score boundaries, identical teams, conflicts, and a finished
time before the start. Repository tests cover duplicate IDs and history eviction.
The repeated public API concurrency test starts two matches sharing a team and
checks that only one succeeds. It uses two tasks, bounded waits, and executor
cleanup. Current tests do not claim exhaustive concurrency coverage; some larger
AI-generated concurrency tests were removed during simplification.

DTOs, converters, Lombok getters, and obvious final-field behavior have no
separate trivial test suites. The author removed an immutability test and rejected
an oversized factory test class. Parameterized tests remain where cases express
business boundaries; they were not kept merely to multiply equivalent examples.
The current factory test intentionally uses capacity two and three finishes.

Maven checks were run repeatedly during implementation. The earlier record
reports checks on JDK 17, 21, and 25, with some later runs on Java 21 targeting
release 17. Available later output also shows Java 25 targeting release 17.
These are historical checks, not proof that every revision ran on all JDKs.
Mockito attachment sometimes failed inside the sandbox; rerunning outside it
allowed the agent to attach. No build configuration change was made for that
restriction. No tests were run for this documentation-only update.

## Guiding artifacts and submission state

The assignment supplied the required operations, ordering example, AI disclosure,
and separate-feature-commit rule. README now supplies the author's concise
feature rationale and accepted trade-offs. Production files establish current
contracts; tests provide concrete examples and evidence of selected behavior.
`pom.xml` defines Java 17 compilation, Lombok annotation processing, and the
JUnit 5/Mockito test dependencies, with no runtime infrastructure dependency.

The earlier AI record also reports official Java and dependency documentation
used during dependency and concurrency work, plus a local public-domain ISO
country-code table and reference checks used for the country list. Exact source
links and the original table were not preserved in that record. This update
retains that provenance statement without inventing citations or claiming a new
external verification of those sources.

The additional history operation is now implemented; the previous statement that
it was deferred is historical. At this review, Git still shows only the initial
and core-feature commits. The assignment's distinct history-feature commit
remains a submission step for the author. Codex has not created it automatically.


## Final senior-level code review

A final review was performed from the perspective of a senior Java developer
assessing an interview assignment. It covered all production and test sources,
Maven configuration, README, the original local Sportradar assignment PDF, and
Git submission history. Existing AI.md sections were not reviewed or rewritten
as part of this review. Only this review record was appended.

`mvn clean verify` passed: 85 tests, no failures or errors, with compiler release
17. This run does not claim testing on every supported JDK. A separate temporary
Java probe reproduced a country-lookup problem under Turkish locale.

No Critical findings or blocking concurrency defects were identified under the
stated consistency guarantees. Two findings were classified as Important:

- `Country` uses default-locale uppercasing for both lookup keys and input.
  Under Turkish locale, `Italy` resolves but `italy` does not; `United States`
  resolves but `UNITED STATES` does not. The smallest correction is
  `Locale.ROOT` in both normalization paths, with a focused regression check.
- The assignment requires a distinct commit introducing the additional feature.
  Available Git history still contains only the initial and core-feature
  commits. The author must complete the separate history-feature commit before
  submission; none was created during this review.

Minor findings concern focused coverage and API documentation. The public
history test currently finishes a 0–0 match and compares history with the returned
view; it does not establish that updated nonzero scores and completion time are
preserved. Extending that test would provide stronger feature coverage without
a new test layer. Zero query limit and invalid storage capacity also lack direct
boundary coverage. README is otherwise accurate and concise, but its general
statement that invalid operations throw `ScoreboardException` omits the
`IllegalArgumentException` used for invalid capacity and completion chronology.
It also describes the temporary removal/insertion gap without stating that a
failure after removal can leave the result permanently absent from both stores.
These are documentation clarifications, not requests for transaction machinery.

The review accepted immutable finished snapshots, bounded insertion-order
history, service-level sorting before limiting, short history locks, and active
reads without global synchronization. It did not classify weakly consistent
summaries, the agreed nontransactional finish flow, timestamp ties, or extremely
unlikely UUID collisions as Important defects. The lightweight DDD/CQRS split is
reasonable for this assignment; no new framework or abstraction was recommended.

Overall, the design and core behavior are suitable for approval after correcting
locale normalization and completing the required feature commit. The minor
test and documentation improvements are recommended. No identified issue was
fixed during this review, and production code, tests, and README were unchanged.

## Final cleanup and author response to the review

After the senior-level review, the author authorized only a small final cleanup.
Country normalization now uses `toUpperCase(Locale.ROOT)` for stored keys and
lookup input, with one focused regression test. The existing public history test
now updates the score to 3–2 before finishing and checks that the final score and
history entry are preserved. Its structure and other working tests were retained.

The author strongly disagreed with the repeated emphasis on `Locale.ROOT`.
AI had raised this issue repeatedly, and the author found that excessive and
frustrating. The author said they were genuinely tired of the recommendation
and eventually agreed to the small technical fix simply to close the discussion.
This was not agreement that country normalization deserved repeated treatment
as a major architectural concern. The author explained that a production system
would normally manage team identities through dedicated reference data and
dictionaries rather than this exercise's simplified country lookup. The issue
is fixed and the author considers the topic closed.

The author also clarified the exception boundary. `ScoreboardException` represents
business errors from public scoreboard operations. `Scoreboards.create(int)` is
a factory, not a business command. Invalid storage capacity is a programming or
configuration error, so `IllegalArgumentException` is deliberate and remains
unchanged. README's general exception statement was narrowed to make this
boundary clear; no new business exception was introduced into the factory.

The author explicitly rejected additional clock handling, stronger synchronization,
transactional coordination, and unnecessary tests. No changes were made to
`Instant.now()`, timestamp validation, repository transactions, or concurrency
behavior. The previously accepted consistency and failure trade-offs remain.
The separate factory test class stays removed, and no extra validation suite or
architectural layer was added. The author will handle the required distinct
history-feature commit; Codex did not create any commit.

Final cleanup verification: `mvn clean verify` passed all 86 tests with no failures
or errors, targeting Java 17. `git diff --check` also passed.
