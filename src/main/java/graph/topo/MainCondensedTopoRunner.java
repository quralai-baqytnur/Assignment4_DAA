package graph.topo;

import graph.core.Graph;
import graph.scc.CondensationGraph;
import graph.scc.KosarajuSCC;
import graph.scc.SCCResult;
import utils.GraphReader;
import utils.Metrics;
import java.nio.file.Path;
import java.util.*;

public class MainCondensedTopoRunner {

    static class DummyMetrics implements Metrics {
        private final Map<String, Long> map = new HashMap<>();
        public void inc(String k, long d) { map.put(k, map.getOrDefault(k, 0L) + d); }
        public long get(String k) { return map.getOrDefault(k, 0L); }
    }

    public static void main(String[] args) throws Exception {
        Graph g = GraphReader.fromJsonFile(Path.of("data/small_cyclic.json"));
        DummyMetrics m = new DummyMetrics();

        KosarajuSCC scc = new KosarajuSCC(g, m);
        SCCResult sccRes = scc.run();

        CondensationGraph cond = new CondensationGraph(g, sccRes);
        Graph dag = cond.buildCondensedGraph();

        TopologicalSort topo = new TopologicalSort(dag, m);
        List<Integer> order = topo.run();

        System.out.println("Topological order of SCC DAG: " + order);
    }
}
