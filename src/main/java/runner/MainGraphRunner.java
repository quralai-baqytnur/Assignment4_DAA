package runner;

import graph.core.Graph;
import graph.scc.KosarajuSCC;
import graph.scc.SCCResult;
import graph.scc.CondensationGraph;
import graph.topo.TopologicalSort;
import graph.dagsp.DagShortestPaths;
import graph.dagsp.DagLongestPath;
import utils.GraphReader;
import utils.Metrics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class MainGraphRunner {

    static class MetricMap implements Metrics {
        private final Map<String, Long> map = new HashMap<>();
        public void inc(String k, long d) { map.merge(k, d, Long::sum); }
        public long get(String k) { return map.getOrDefault(k, 0L); }
    }

    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");
        List<Path> files;
        try (var s = Files.list(dataDir)) {
            files = s.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }

        JSONArray results = new JSONArray();
        for (Path p : files) {
            results.put(processFileToJson(p));
        }

        Files.writeString(Path.of("results.json"), results.toString(2));
        System.out.println("JSON report saved to results.json");
    }

    private static JSONObject processFileToJson(Path path) {
        JSONObject out = new JSONObject();
        MetricMap m = new MetricMap();

        try {
            Graph g = GraphReader.fromJsonFile(path);
            out.put("file", path.getFileName().toString());
            out.put("n", g.n());
            out.put("m", g.edges().size());

            KosarajuSCC sccAlgo = new KosarajuSCC(g, m);
            SCCResult scc = sccAlgo.run();

            List<List<Integer>> comps = scc.getComponents();
            List<Integer> sizes = comps.stream().map(List::size).collect(Collectors.toList());

            JSONObject sccJson = new JSONObject();
            sccJson.put("count", scc.getCount());
            sccJson.put("sizes", new JSONArray(sizes));
            JSONArray compsArr = new JSONArray();
            for (List<Integer> comp : comps) compsArr.put(new JSONArray(comp));
            sccJson.put("components", compsArr);
            out.put("scc", sccJson);

            CondensationGraph cond = new CondensationGraph(g, scc);
            Graph dagCondensed = cond.buildCondensedGraph();
            TopologicalSort topoOnCondensed = new TopologicalSort(dagCondensed, m);
            List<Integer> orderCondensed = topoOnCondensed.run();

            JSONObject condJson = new JSONObject();
            condJson.put("dag_nodes", dagCondensed.n());
            condJson.put("dag_edges", dagCondensed.edges().size());
            condJson.put("topo_order", new JSONArray(orderCondensed));
            condJson.put("pushes", m.get("pushes"));
            condJson.put("pops", m.get("pops"));
            out.put("condensation", condJson);

            if (path.getFileName().toString().contains("_dag.json")) {
                TopologicalSort topoOnOriginal = new TopologicalSort(g, m);
                List<Integer> topoOrder = topoOnOriginal.run();
                int source = (g.source() != null) ? g.source() : 0;

                DagShortestPaths sp = new DagShortestPaths(g, m);
                DagShortestPaths.Result spRes = sp.run(topoOrder, source);

                DagLongestPath lp = new DagLongestPath(g, m);
                DagLongestPath.Result lpRes = lp.run(topoOrder, source);

                int tSp = g.n() - 1;
                List<Integer> spPath = spRes.reconstructPath(tSp);

                int tLongest = lpRes.argMax();
                List<Integer> lpPath = lpRes.reconstructPath(tLongest);
                long lpLen = (lpPath.isEmpty() ? Long.MIN_VALUE / 2 : lpRes.best[tLongest]);

                JSONObject dagJson = new JSONObject();
                dagJson.put("source", source);
                dagJson.put("topo_order", new JSONArray(topoOrder));

                JSONObject shortest = new JSONObject();
                shortest.put("distances", new JSONArray(spRes.dist));
                shortest.put("example_target", tSp);
                shortest.put("example_path", new JSONArray(spPath));
                dagJson.put("shortest", shortest);

                JSONObject longest = new JSONObject();
                longest.put("argmax_target", tLongest);
                longest.put("path", new JSONArray(lpPath));
                longest.put("length", lpPath.isEmpty() ? JSONObject.NULL : lpLen);
                dagJson.put("longest", longest);

                JSONObject metrics = new JSONObject();
                metrics.put("pushes", m.get("pushes"));
                metrics.put("pops", m.get("pops"));
                metrics.put("relax_sp", m.get("relaxations_sp"));
                metrics.put("relax_lp", m.get("relaxations_lp"));
                metrics.put("time_sp_ns", m.get("time_ns_dag_sp"));
                metrics.put("time_longest_ns", m.get("time_ns_dag_longest"));
                dagJson.put("metrics", metrics);

                out.put("dag", dagJson);
            }

        } catch (Exception e) {
            out.put("error", e.getMessage());
        }

        return out;
    }
}
