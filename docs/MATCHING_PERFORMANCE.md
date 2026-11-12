# Lyrics matching performance comparison

Measured on November 11, 2026. Baseline production: `659c37f`; changed production:
`382e440`. The same final synthetic benchmark harness was used on both builds.

## Method

- Desktop OpenJDK 17, 256 MiB JVM heap per benchmark, three fresh JVMs per build;
  before/after order alternates. Reported cells are the median of three per-run
  percentiles, not a confidence interval. No Gradle compilation runs concurrently.
- CPU: 8 candidates with 60 rows each, 75% distinct row texts; 2,400 mixed warmups
  followed by 1,000 measurements per case. Synthetic fixtures contain no private
  playlist data or full song lyrics.
- Lookup: actual LRCLIB provider and MockWebServer, fixed 80 ms response-header
  delay, connection close after each reply, one discarded warmup and 8 measured
  lookups per scenario. The server returns identical metadata/text on both builds.
- The before production classes were frozen before implementation. Only the
  final benchmark classes/build tasks were shared with the baseline checkout.

## Warm local selection

| Scenario | Before p50 / p95 (ms) | After p50 / p95 (ms) |
| --- | ---: | ---: |
| Ordinary English | 0.245 / 0.469 | 0.409 / 0.715 |
| Japanese + English / romanization alternatives | 0.166 / 0.289 | 0.335 / 0.623 |
| Traditional/Simplified title | 0.025 / 0.052 | 0.094 / 0.179 |
| Featured title credit | 0.052 / 0.104 | 0.421 / 0.734 |
| English version spelling | 0.277 / 0.442 | 0.482 / 0.874 |
| Cyrillic lyric rows | 0.237 / 0.422 | 0.117 / 0.244 |

The Traditional/Simplified and featured-title baseline cases returned no winner.
Afterward they validate and inspect lyric payloads, so their ratios include newly
performed useful work. Ordinary selection adds about 0.165 ms at the median; all
measured warm p95 values remain below 0.9 ms. These are desktop JVM measurements,
not Android device or frame-time guarantees.

First selection in a fresh JVM (Traditional/Simplified fixture, including class/table
initialization and JIT cold effects) rose from
31.5 to 57.9 ms. This cost is not repeated on every song.

## Controlled LRCLIB lookup

| Scenario | Requests before → after | Emitted candidates before → after | p50 before → after (ms) | p95 before → after (ms) |
| --- | ---: | ---: | ---: | ---: |
| Exact ordinary match | 1 → 1 | 1 → 1 | 87.2 → 88.3 | 89.6 → 91.6 |
| Suspected romanization, original available | 1 → 2 | 1 → 2 | 86.3 → 176.6 | 88.6 → 179.1 |
| Exact miss, structured search succeeds | 3 → 3 | 1 → 1 | 255.6 → 255.9 | 259.0 → 257.9 |
| No usable result | 5 → 5 | 0 → 0 | 417.3 → 418.0 | 420.0 → 422.4 |

Ordinary, structured fallback and miss request budgets are unchanged. The optional
romanization query adds about one response delay (90 ms here), with a one-second
coroutine deadline in production. Timeout/503 tests verify that the healthy exact
result survives. Extra discovery emits up to three writing variants in general;
the controlled scenario emits two. NetEase retains its existing bounded search
and at most three lyric fetches; better matching can legitimately fetch a lyric
where the old matcher rejected every candidate.

The coordinator still waits for concurrently enabled providers. Public-service
latency, server availability, Android scheduling and other providers were not
included in the controlled timing comparison. No claim of identical full-app
lookup time or physical-device verification is made. Cache hits still bypass
selection/network; policy-2 winners require one fresh lookup under policy 3.

## Reproduce

Run the following opt-in tasks with JDK 17 on an otherwise idle machine:

```sh
./gradlew :provider:selection:matchingBenchmark :provider:lrclib:lookupBenchmark \
  --max-workers=1 --no-parallel --no-daemon \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8" \
  -Pkotlin.compiler.execution.strategy=in-process
```

For before/after measurements, use isolated checkouts and copy the same final
`MatchingBenchmark.kt`, `LookupBenchmark.kt` and the two benchmark task definitions
into the baseline. Leave production sources unchanged. Run each benchmark in a
fresh JVM three times, alternating build order. On the workstation use the
documented verified 3 GiB / zero-swap systemd scope; never compile in parallel
with benchmarks or an emulator. Timing output is diagnostic, not a flaky CI
pass/fail assertion. Functional tests assert winners, request counts, failure
fallback, version/artist rejection and the optional deadline separately.
