package graph.topo;

import graph.core.Graph;
import utils.GraphReader;
import utils.Metrics;

import java.nio.file.Path;
import java.util.*;

public class MainTopoRunner {

    static class DummyMetrics implements Metrics {
        private final Map<String, Long> map = new HashMap<>();
        public void inc(String k, long d) { map.put(k, map.getOrDefault(k, 0L) + d); }
        public long get(String k) { return map.getOrDefault(k, 0L); }
    }

    public static void main(String[] args) throws Exception {
        Graph g = GraphReader.fromJsonFile(Path.of("data/small_dag.json"));
        DummyMetrics m = new DummyMetrics();

        TopologicalSort topo = new TopologicalSort(g, m);
        List<Integer> order = topo.run();

        System.out.println("=== Topological Sort ===");
        System.out.println("Order: " + order);
        System.out.println("Pushes: " + m.get("pushes"));
        System.out.println("Pops: " + m.get("pops"));
        System.out.println("Edges relaxed: " + m.get("edges_relaxed"));
    }
}
