package graph.scc;

import graph.core.Graph;
import utils.Metrics;
import java.util.*;

public class KosarajuSCC {

    private final Graph g;
    private final Metrics metrics;

    public KosarajuSCC(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
    }

    public SCCResult run() {
        int n = g.n();
        boolean[] visited = new boolean[n];
        Deque<Integer> order = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs1(i, visited, order);
        }

        Arrays.fill(visited, false);
        List<List<Integer>> comps = new ArrayList<>();

        while (!order.isEmpty()) {
            int v = order.pop();
            if (!visited[v]) {
                List<Integer> comp = new ArrayList<>();
                dfs2(v, visited, comp);
                comps.add(comp);
            }
        }

        return new SCCResult(comps);
    }

    private void dfs1(int v, boolean[] visited, Deque<Integer> order) {
        visited[v] = true;
        metrics.inc("dfs1_calls", 1);
        for (int[] e : g.adjOut().get(v)) {
            int to = e[0];
            if (!visited[to]) dfs1(to, visited, order);
        }
        order.push(v);
    }

    private void dfs2(int v, boolean[] visited, List<Integer> comp) {
        visited[v] = true;
        comp.add(v);
        metrics.inc("dfs2_calls", 1);
        for (int[] e : g.adjIn().get(v)) {
            int from = e[0];
            if (!visited[from]) dfs2(from, visited, comp);
        }
    }
}
