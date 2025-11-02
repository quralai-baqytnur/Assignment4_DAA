package graph.scc;

import graph.core.Graph;
import graph.topo.TopologicalSort;
import utils.GraphReader;
import utils.Metrics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class M2 implements Metrics {
    private final Map<String, Long> m = new HashMap<>();
    public void inc(String k, long d){ m.merge(k, d, Long::sum); }
    public long get(String k){ return m.getOrDefault(k,0L); }
}

public class CondenseAllCyclicTest {

    static Stream<Arguments> cyclicOrMultiFiles() throws Exception {
        Path dataDir = Path.of("data");
        assertTrue(Files.exists(dataDir), "data dir not found: " + dataDir.toAbsolutePath());

        List<Path> list = Files.list(dataDir)
                .filter(p -> {
                    String name = p.getFileName().toString();
                    return name.endsWith("_cyclic.json") || name.endsWith("_multi.json");
                })
                .sorted()
                .collect(Collectors.toList());

        assertFalse(list.isEmpty(), "No *_cyclic.json or *_multi.json found in: " + dataDir.toAbsolutePath());
        return list.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("cyclicOrMultiFiles")
    void condensationIsDag(Path path) throws Exception {
        Graph g = GraphReader.fromJsonFile(path);
        M2 m = new M2();

        var scc = new KosarajuSCC(g, m).run();
        var cond = new CondensationGraph(g, scc);
        Graph dag = cond.buildCondensedGraph();

        var order = new TopologicalSort(dag, m).run();
        assertEquals(dag.n(), order.size(), "condensed DAG topo broken for " + path);

        System.out.println("[COND] " + path.getFileName() +
                " | scc_count=" + scc.getCount() +
                ", dag_nodes=" + dag.n() +
                ", dag_edges=" + dag.edges().size() +
                ", dfs1_calls=" + m.get("dfs1_calls") +
                ", dfs2_calls=" + m.get("dfs2_calls"));
    }
}
