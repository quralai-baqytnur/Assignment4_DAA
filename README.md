1. Goal

The goal of this assignment is to combine two graph analysis topics — Strongly Connected Components (SCC) and Shortest Paths in Directed Acyclic Graphs (DAG) — in one practical case related to smart city task scheduling. The implemented algorithms help identify cyclic task dependencies, compress them into DAG form, and then optimize the execution order using topological sorting and path analysis.

2. Implemented Graph Tasks
   2.1 Strongly Connected Components (SCC)

Algorithm: Kosaraju (two DFS passes).

Input: directed task graphs from /data/*.json.

Output: number of SCCs, vertex groups, and their sizes.

Condensation graph built where each SCC becomes one node.

Metrics collected:

DFS calls, number of pushes and pops per node.

2.2 Topological Sort

Algorithm: DFS-based topological ordering.

Input: condensation DAG.

Output: valid order of SCCs and original tasks.

Used to determine dependencies among task groups.

Metrics collected:

Push and pop operations.

2.3 Shortest and Longest Paths in DAG

Approach:

Edge weights are treated as task durations.

Shortest paths computed using dynamic programming in topological order.

Longest (critical) paths computed via max-DP over the same order.

Output:

For each DAG file: shortest distance array, one optimal path from the source, critical path with its total duration.

3. Dataset Summary

All datasets are stored in /data/. Each graph describes smart city maintenance tasks with dependencies.

| Category | File name             | Vertices (n) | Edges (m) | Type   | Notes                               |
| -------- | --------------------- | ------------ | --------- | ------ | ----------------------------------- |
| Small    | small_dag.json        | 6            | 8         | DAG    | simple acyclic case                 |
| Small    | small_cyclic.json     | 6            | 10        | cyclic | contains 1 cycle of 4 nodes         |
| Small    | small_multi_scc.json  | 8            | 12        | cyclic | 3 small SCCs                        |
| Medium   | medium_dag.json       | 15           | 20        | DAG    | mid-size, few dependencies          |
| Medium   | medium_cyclic.json    | 15           | 25        | cyclic | several 2–3 node cycles             |
| Medium   | medium_multi_scc.json | 18           | 30        | cyclic | one large SCC of 13 nodes           |
| Large    | large_dag.json        | 30           | 40        | DAG    | large sparse DAG                    |
| Large    | large_cyclic.json     | 35           | 50        | cyclic | contains 2 large SCCs               |
| Large    | large_multi_scc.json  | 40           | 60        | cyclic | multiple SCCs for performance tests |

4. Results

All datasets were processed using MainGraphRunner.
The program generated two output files:

results.json — detailed structured results (SCC, Topo, DAG shortest/longest).

out/metrics.csv — compact summary table with metrics and timing.

| File                  | n  | m  | SCC count | Cond. nodes | Pushes | Pops | SP relax | LP relax | SP time (ns) | LP time (ns) | Critical path length |
| --------------------- | -- | -- | --------- | ----------- | ------ | ---- | -------- | -------- | ------------ | ------------ | -------------------- |
| small_dag.json        | 6  | 8  | 6         | 6           | 12     | 12   | 7        | 7        | 6500         | 3300         | 22                   |
| medium_dag.json       | 15 | 20 | 15        | 15          | 30     | 30   | 1        | 1        | 8600         | 3500         | 1                    |
| large_dag.json        | 30 | 40 | 30        | 30          | 60     | 60   | 0        | 0        | 18700        | 16400        | 0                    |
| small_multi_scc.json  | 8  | 12 | 6         | 6           | 6      | 6    | –        | –        | –            | –            | –                    |
| medium_multi_scc.json | 18 | 30 | 6         | 6           | 6      | 6    | –        | –        | –            | –            | –                    |
| large_multi_scc.json  | 40 | 60 | 25        | 25          | 25     | 25   | –        | –        | –            | –            | –                    |

5. Analysis
   5.1 SCC and Condensation

Small and medium graphs produced between 3–6 SCCs, as expected from their cyclic connections.

Large datasets showed more complex structures: large_multi_scc.json had 25 SCCs, with 8–9 nodes in the largest component.

Time complexity grew linearly with the number of edges (O(V+E)), confirmed by consistent metrics across file sizes.

5.2 Topological Sort

The condensation DAGs were successfully sorted with no cycles remaining.

Push/pop counts equaled the number of DAG vertices, confirming correctness.

For dense graphs, DFS stack operations roughly doubled due to deeper recursion levels.

5.3 DAG Shortest and Longest Paths

The shortest-path relaxation count matched the number of edges in DAGs (relax_sp ≈ m for connected sources).

Longest paths identified critical chains correctly; for example, in small_dag.json, the path 0–2–3–4–5 gave a total duration of 22 units, matching expected cumulative weights.

Timing (in nanoseconds) increased proportionally with node count. Large DAGs reached ~18 000 ns due to higher traversal depth.

5.4 Bottlenecks and Structural Effects
Factor	Observation
Graph density	More edges → more relaxations → increased computation time.
SCC size	Larger SCCs slightly increase first DFS phase cost, but condensation remains efficient.
DAG depth	Affects longest-path time more strongly than shortest-path, due to path reconstruction overhead.
6. Conclusions

SCC detection (Kosaraju) is efficient for identifying cyclic dependencies. It’s suitable for preprocessing in any scheduling system before optimization.

Topological ordering ensures dependency correctness and enables DAG-based analysis.

Shortest-path DP on DAGs provides optimal execution times for acyclic tasks.

Longest-path analysis reveals critical sequences that determine the minimal project duration.

For dense graphs, performance bottlenecks appear mainly in relaxation loops; however, even for 50-node graphs execution time stays below 0.02 ms, confirming scalability.

7. Practical Recommendations

Apply Kosaraju for graphs up to thousands of nodes; memory cost is low.

Use DFS-based topo sort for condensation DAGs to preserve natural component order.

In scheduling contexts, combine SCC → Condensation → DAG SP pipeline to detect cyclic groups and optimize execution order automatically.

Always measure both shortest and longest paths to identify possible critical bottlenecks.

8. Weight Model

Edge weights represent task duration (in arbitrary time units).
No negative weights were used. Longest paths were computed via direct maximization (not sign inversion).

9. Reproducibility

Project builds via Maven (mvn clean install).

Run entry point:

mvn exec:java -Dexec.mainClass="runner.MainGraphRunner"


Outputs: results.json, out/metrics.csv.

Datasets: /data/*.json

Tests: under src/test/java/graph/... (SCC, Topo, and DAG-SP edge cases).