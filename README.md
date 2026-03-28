# LLM-Racer: Engineering for P99 Reliability
### High-Concurrency AI Inference Benchmarking & Resilience Patterns

**LLM-Racer** is a performance-testing sandbox designed to simulate the unpredictable latency and failure modes of Large Language Model (LLM) inference. This project benchmarks **Java 21 Virtual Threads** against traditional **Platform Threads** under heavy chaos, implementing industry-standard resilience patterns to mitigate "tail latency" (P99).

## The Mission
AI Inference is non-deterministic. A single request might take 200ms, while the next (due to KV-cache eviction or GPU contention) takes 5s. LLM-Racer explores how to "shave the tail" using:
* **Hedged Requests** at P90 to fight jitter.
* **Virtual Threads (Project Loom)** to scale I/O-bound concurrency.
* **Resilience4j** for Circuit Breaking and Bulkheading.
* **Chaos Engineering** (Latency injection and failure rates).

---

## Performance Deep Dive
I conducted a "Stress-to-Failure" analysis comparing threading models and resilience configurations.

### 1. The Threading "Cliff" (No Resilience)
| Metric | Platform Threads (5k VUs) | Virtual Threads (5k VUs) |
| :--- | :--- | :--- |
| **Success Rate** | **0.07% (System Collapse)** | **99.66%** |
| **Throughput** | 832 req/s | 5,500 req/s |
| **P95 Latency** | 4.61s | 1.76s |

**Analysis:** Platform threads hit a context-switching wall at 5k concurrent requests. Virtual Threads maintained high throughput by decoupling the execution from OS-level thread limits.

### 2. Tail Latency Mitigation (With Resilience)
By implementing **Hedged Requests at P90**, the system initiates a secondary "racing" request if the first is delayed. 
* **P95 Improvement:** Reduced from 1.76s to **1.63s** under 5k load.
* **The Trade-off:** Hedging introduces a **Retry Amplification Factor** (approx. 1.1x load), which requires careful Bulkhead tuning to prevent a "Death Spiral" at 20k+ VUs.

---

## Features & Chaos Injections
To simulate a production AI environment, the following stressors are injected:
* **10% Latency:** +1s delay (Simulates cold-starts).
* **5% Failure:** 503 Service Unavailable (Simulates rate limits).
* **2% Timeout:** Requests dropped (Simulates network partitions).

### Resilience Stack:
* **Circuit Breaker:** Prevents cascading failures when the inference backend is saturated.
* **Bulkhead Isolation:** Limits concurrent execution to prevent resource exhaustion.
* **Retry with Jitter:** Smooths out recovery after transient failures.
* **Deadline Propagation:** Ensures timed-out requests don't waste downstream GPU cycles.

---

## Quantitative Formulas Used
This project applies systems engineering math to validate results:

* **Little’s Law:** $L = \lambda W$ (Validating system capacity vs. arrival rate).
* **Tail Amplification:** $1 - (1-p)^n$ (Calculating how a 5% failure rate scales across multi-step LLM chains).
* **Retry Amplification Factor:** $\frac{\text{Total Egress Requests}}{\text{Total Ingress Requests}}$

---

## Setup & Reproduction
1. **Prerequisites:** Java 21+, Maven.
2. **Build:** `mvn clean package`
3. **Run Chaos Test:** 
   # Example: Running 5k VUs with Virtual Threads
   
   ```
   k6 run scripts/stress-test.js --vus 5000 --duration 2m
   ```

---

## Learning Observations
> "Resilience is not a free lunch. Misconfigured Bulkheads or aggressive Hedging can actually decrease throughput. An AI Platform Engineer's job is to find the equilibrium between reliability and resource cost."

---
