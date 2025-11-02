package graph.dagsp;

import graph.core.Graph;
import utils.Metrics;
import java.util.*;

public class DagLongestPath {
    private final Graph g;
    private final Metrics metrics;

    public DagLongestPath(Graph g, Metrics metrics) { this.g = g; this.metrics = metrics; }

    public static class Result {
        public final int source;
        public final long[] best;
        public final int[] parent;
        public Result(int source, long[] best, int[] parent) {
            this.source = source; this.best = best; this.parent = parent;
        }
        public List<Integer> reconstructPath(int t) {
            if (best[t] == Long.MIN_VALUE / 2) return Collections.emptyList();
            LinkedList<Integer> path = new LinkedList<>();
            for (int v = t; v != -1; v = parent[v]) path.addFirst(v);
            return path;
        }
        public int argMax() {
            int idx = 0;
            for (int i = 1; i < best.length; i++)
                if (best[i] > best[idx]) idx = i;
            return idx;
        }
    }

    public Result run(List<Integer> topoOrder, int source) {
        int n = g.n();
        long NEG = Long.MIN_VALUE / 2;
        long[] best = new long[n];
        int[] parent = new int[n];
        Arrays.fill(best, NEG);
        Arrays.fill(parent, -1);
        best[source] = 0;

        long t0 = System.nanoTime();
        for (int u : topoOrder) {
            if (best[u] == NEG) continue;
            for (int[] e : g.adjOut().get(u)) {
                int v = e[0], w = e[1];
                long cand = best[u] + w;
                metrics.inc("relaxations_lp", 1);
                if (cand > best[v]) { best[v] = cand; parent[v] = u; }
            }
        }
        long t1 = System.nanoTime();
        metrics.inc("time_ns_dag_longest", (t1 - t0));
        return new Result(source, best, parent);
    }
}
