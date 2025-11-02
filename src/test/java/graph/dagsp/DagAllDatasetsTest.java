package graph.dagsp;

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

class M implements Metrics {
    private final Map<String, Long> m = new HashMap<>();
    public void inc(String k, long d){ m.merge(k, d, Long::sum); }
    public long get(String k){ return m.getOrDefault(k,0L); }
}

public class DagAllDatasetsTest {

    static Stream<Arguments> dagFiles() throws Exception {
        Path dataDir = Path.of("data");
        assertTrue(Files.exists(dataDir), "data dir not found: " + dataDir.toAbsolutePath());

        List<Path> list = Files.list(dataDir)
                .filter(p -> p.getFileName().toString().endsWith("_dag.json"))
                .sorted()
                .collect(Collectors.toList());

        assertFalse(list.isEmpty(), "No *_dag.json files found in: " + dataDir.toAbsolutePath());
        return list.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dagFiles")
    void topoAndDagSp(Path path) throws Exception {
        Graph g = GraphReader.fromJsonFile(path);
        assertTrue(g.isDirected(), "must be directed: " + path);

        M metrics = new M();
        var topo = new TopologicalSort(g, metrics).run();
        assertEquals(g.n(), topo.size(), "topo size != n for " + path);

        int s = (g.source()!=null) ? g.source() : 0;

        var sp = new DagShortestPaths(g, metrics).run(topo, s);
        assertEquals(0L, sp.dist[s], "source dist must be 0: " + path);

        var lp = new DagLongestPath(g, metrics).run(topo, s);
        assertTrue(lp.best[s] >= 0, "longest[source] invalid in " + path);

        System.out.println("[DAG] " + path.getFileName() +
                " | pushes=" + metrics.get("pushes") +
                ", pops=" + metrics.get("pops") +
                ", relax_sp=" + metrics.get("relaxations_sp") +
                ", relax_lp=" + metrics.get("relaxations_lp") +
                ", time_sp_ns=" + metrics.get("time_ns_dag_sp") +
                ", time_longest_ns=" + metrics.get("time_ns_dag_longest"));
    }
}
