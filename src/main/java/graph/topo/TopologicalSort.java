package graph.topo;

import graph.core.Graph;
import utils.Metrics;
import java.util.*;

public class TopologicalSort {

    private final Graph g;
    private final Metrics metrics;

    public TopologicalSort(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
    }

    public List<Integer> run() {
        int n = g.n();
        int[] indeg = new int[n];
        for (int v = 0; v < n; v++) {
            for (int[] edge : g.adjOut().get(v)) {
                indeg[edge[0]]++;
            }
        }

        Queue<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                q.add(v);
                metrics.inc("pushes", 1);
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.poll();
            metrics.inc("pops", 1);
            topoOrder.add(u);

            for (int[] edge : g.adjOut().get(u)) {
                int v = edge[0];
                indeg[v]--;
                metrics.inc("edges_relaxed", 1);
                if (indeg[v] == 0) {
                    q.add(v);
                    metrics.inc("pushes", 1);
                }
            }
        }

        if (topoOrder.size() != n) {
            throw new IllegalStateException("Graph is not a DAG (cycle detected)");
        }

        return topoOrder;
    }
}
