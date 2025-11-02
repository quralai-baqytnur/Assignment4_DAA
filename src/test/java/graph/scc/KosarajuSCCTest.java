package graph.scc;

import graph.core.Graph;
import utils.GraphReader;
import utils.Metrics;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DummyMetrics implements Metrics {
    private final Map<String, Long> map = new HashMap<>();
    public void inc(String k, long d) { map.put(k, map.getOrDefault(k, 0L) + d); }
    public long get(String k) { return map.getOrDefault(k, 0L); }
}

public class KosarajuSCCTest {
    @Test
    void testSmallCyclicGraph() throws Exception {
        Graph g = GraphReader.fromJsonFile(Path.of("data/small_cyclic.json"));
        DummyMetrics m = new DummyMetrics();
        KosarajuSCC scc = new KosarajuSCC(g, m);
        SCCResult res = scc.run();

        assertTrue(res.getCount() >= 1);
        assertTrue(m.get("dfs1_calls") > 0);
        assertTrue(m.get("dfs2_calls") > 0);
    }
}
