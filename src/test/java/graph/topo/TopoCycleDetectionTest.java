package graph.topo;

import graph.core.Graph;
import utils.GraphReader;
import utils.Metrics;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DummyMetrics implements Metrics {
    public void inc(String k, long d) {}
    public long get(String k) { return 0; }
}

public class TopoCycleDetectionTest {

    @Test
    void shouldThrowIfGraphHasCycle() throws Exception {
        Graph g = GraphReader.fromJsonFile(Path.of("data/small_cyclic.json"));
        DummyMetrics m = new DummyMetrics();

        TopologicalSort topo = new TopologicalSort(g, m);
        assertThrows(IllegalStateException.class,
                topo::run,
                "TopologicalSort must throw exception on cyclic graph");
    }
}
