---
name: java-performance-tuning
description: Expert playbook for memory optimization, garbage collection efficiency, thread safety, profiling, and latency reduction in modern Java (21+) and Minecraft Paper server environments.
---

# Java Performance Tuning & Optimization Playbook

> **Target Platform:** Java 21 / 25
> **Scope:** High-concurrency systems, low-latency hot paths, memory leak mitigation, and Bukkit/Paper ticking loop optimization.

---

## 1. Core Principles (Always Enforce)

1. **Never guess, always profile.** Do not perform speculative optimization. If a method is not a measured hot path (via JProfiler, Spark, or async-profiler), prioritize readability and safety over micro-optimizations.
2. **Minimize Object Allocation in Hot Paths.** Object allocations trigger GC cycles. In ticking loops or packet handlers:
   - Reuse mutable containers (e.g. pre-allocated vectors, recyclers).
   - Use primitive collections (like Fastutil or Trove) instead of boxed wrappers (`int` instead of `Integer`, `long` instead of `Long`).
   - Limit string concatenation (`+` in loops); use `StringBuilder` or pre-compile formatters.
3. **Prevent Memory Leaks.** Java is garbage-collected but reference-leaked:
   - Always clear static collections and caches.
   - Use weak references (`WeakHashMap`, `WeakReference`) for session mapping or entity tracking where lifecycle is managed elsewhere.
   - Cancel Bukkit tasks and unregister listeners on plugin disable or game termination.
4. **Thread Safety Without Bottlenecks.** Bypassing synchronized bottlenecks:
   - Use non-blocking algorithms (`AtomicInteger`, `AtomicReference`, `LongAdder`).
   - Use `ConcurrentHashMap` with proper initial capacities.
   - Prefer read-write locks (`ReentrantReadWriteLock` or Java 8+ `StampedLock`) for data structures that are frequently read but rarely written.

---

## 2. JVM & Garbage Collection Optimizations

### Heap Allocation Strategies
- Keep heap allocations aligned with the server hardware. For large Minecraft servers, use G1GC or ZGC:
  - **G1GC Flag Defaults**: `-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1ReservePercent=15`
  - **ZGC Flag Defaults** (Ultra-low latency): `-XX:+UseZGC -XX:+ZGenerational`
- Avoid memory fragmentation by using matching minimum and maximum heap limits: `-Xms8G -Xmx8G`.

### CPU-Friendly Patterns
- **Loop Vectorization**: Write clean, standard loops that the JIT compiler can auto-vectorize into SIMD instructions. Avoid complex branching inside tight loops.
- **Escape Analysis**: Keep local variables local. If an object doesn't escape the scope of the method, the JVM can allocate it on the stack (Scalar Replacement) instead of the heap.

---

## 3. Minecraft/Paper Specific Hot Paths

### 3.1 Ticking Loop Optimizations
The main thread runs at 20 ticks per second (50ms budget per tick). Any execution taking >5ms inside a single tick risks server lag (TPS drop).
- **Asynchronous Delegations**: Run heavy computing (database queries, network requests, JSON parsing, file I/O) on async threads using scheduler tasks or CompletableFutures.
- **Entity Location Checks**: Locating entities in a radius is expensive.
  - **DO NOT** use `world.getEntities().stream().filter(...)` (fetches all entities in the world).
  - **DO** use `world.getNearbyEntities(location, x, y, z)` or `Location#getNearbyEntities(x, y, z)`.

### 3.2 Chunk Loading & Blocks
- **Block Updates**: Bypassing physics ticks during block setting:
  - Use `BlockState#setBlockData(data, false)` to avoid triggering cascading block updates.
- **Asynchronous Chunk Checks**:
  - Always check if a chunk is loaded before accessing blocks: `world.isChunkLoaded(x, z)`.
  - Use asynchronous chunk loading where possible: `world.getChunkAtAsync(loc)`.

---

## 4. Performance Tuning Checklist

Use this checklist during code creation and review:

- [ ] **No Boxed Primitives**: Primitive fields and collections (`int`, `double`, `boolean`) are used instead of wrappers (`Integer`, `Double`, `Boolean`).
- [ ] **Zero-Allocation Loops**: Loops running every tick do not allocate new objects (`new ItemStack()`, `new Location()`, `new Vector()`).
- [ ] **Async I/O**: Database, web, and config file I/O operations are run outside the main thread using async tasks.
- [ ] **Weak Referencing**: Session stores and registries mapping players/entities use `UUID` keys or wrap values in weak references to prevent memory retention after departure.
- [ ] **No Stream Overhead in Hot Paths**: Legacy for-loops are used in ticking methods rather than stream filters/mappings for optimal throughput.
- [ ] **Preflight Chunk Validation**: All spatial operations check if chunks are loaded before block modification or entity searches.
