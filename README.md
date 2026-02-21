# llm-racer
Java 21 LLM racing service built with virtual threads and structured concurrency. Uses ScopedValue for context propagation, StructuredTaskScope.ShutdownOnSuccess for first-response-wins orchestration, and tracks cancellation effectiveness under deadlines.
