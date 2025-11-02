package graph.scc;

import graph.core.Graph;
import utils.GraphReader;
import utils.Metrics;

import java.nio.file.Path;
import java.util.*;

public class MainSCCRunner {

    static class DummyMetrics implements Metrics {
        private final Map<String, Long> map = new HashMap<>();
        public void inc(String k, long d) { map.put(k, map.getOrDefault(k, 0L) + d); }
        public long get(String k) { return map.getOrDefault(k, 0L); }
    }

    public static void main(String[] args) throws Exception {
        Graph g = GraphReader.fromJsonFile(Path.of("data/small_cyclic.json"));
        DummyMetrics m = new DummyMetrics();

        KosarajuSCC scc = new KosarajuSCC(g, m);
        SCCResult res = scc.run();

        System.out.println("SCC Analysis");
        System.out.println("Components count: " + res.getCount());
        System.out.println("Components: " + res.getComponents());
        System.out.println("DFS1 calls: " + m.get("dfs1_calls"));
        System.out.println("DFS2 calls: " + m.get("dfs2_calls"));

        CondensationGraph cg = new CondensationGraph(g, res);
        Graph dag = cg.buildCondensedGraph();

        System.out.println("\nCondensation DAG");
        System.out.println("DAG nodes (SCCs): " + dag.n());
        System.out.println("DAG edges: " + dag.edges().size());
        cg.printCondensationSummary(dag);
    }
}
