package graph.dagsp;
import graph.core.Graph;
import utils.Metrics;
import java.util.*;

public class DagPaths {

    private final Graph dag;
    private final Metrics metrics;

    public DagPaths(Graph dag, Metrics metrics) {
        this.dag = dag;
        this.metrics = metrics;
    }

    public int[] shortestPaths(int source) {
        int n = dag.n();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        int[] prev = new int[n];
        Arrays.fill(prev, -1);

        List<Integer> topo = new graph.topo.TopologicalSort(dag, metrics).run();
        for (int u : topo) {
            if (dist[u] == Integer.MAX_VALUE) continue;
            for (int[] edge : dag.adjOut().get(u)) {
                int v = edge[0], w = edge[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                    metrics.inc("relaxed_edges", 1);
                }
            }
        }

        return dist;
    }

    public int[] longestPaths(int source) {
        int n = dag.n();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        dist[source] = 0;

        int[] prev = new int[n];
        Arrays.fill(prev, -1);

        List<Integer> topo = new graph.topo.TopologicalSort(dag, metrics).run();
        for (int u : topo) {
            if (dist[u] == Integer.MIN_VALUE) continue;
            for (int[] edge : dag.adjOut().get(u)) {
                int v = edge[0], w = edge[1];
                if (dist[u] + w > dist[v]) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                    metrics.inc("relaxed_edges", 1);
                }
            }
        }

        return dist;
    }

    public static List<Integer> reconstructPath(int[] prev, int target) {
        List<Integer> path = new ArrayList<>();
        for (int at = target; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
