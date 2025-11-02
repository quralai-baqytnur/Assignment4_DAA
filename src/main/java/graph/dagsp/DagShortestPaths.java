package graph.dagsp;

import graph.core.Graph;
import utils.Metrics;
import java.util.*;

public class DagShortestPaths {
    private final Graph g;
    private final Metrics metrics;

    public DagShortestPaths(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
    }

    public static class Result {
        public final int source;
        public final long[] dist;
        public final int[] parent;
        public Result(int source, long[] dist, int[] parent) {
            this.source = source; this.dist = dist; this.parent = parent;
        }
        public List<Integer> reconstructPath(int t) {
            if (dist[t] == Long.MAX_VALUE) return Collections.emptyList();
            LinkedList<Integer> path = new LinkedList<>();
            for (int v = t; v != -1; v = parent[v]) path.addFirst(v);
            return path;
        }
    }

    public Result run(List<Integer> topoOrder, int source) {
        int n = g.n();
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        long t0 = System.nanoTime();
        for (int u : topoOrder) {
            if (dist[u] == Long.MAX_VALUE) continue;
            for (int[] e : g.adjOut().get(u)) {
                int v = e[0], w = e[1];
                long cand = dist[u] + w;
                metrics.inc("relaxations_sp", 1);
                if (cand < dist[v]) { dist[v] = cand; parent[v] = u; }
            }
        }
        long t1 = System.nanoTime();
        metrics.inc("time_ns_dag_sp", (t1 - t0));
        return new Result(source, dist, parent);
    }
}
