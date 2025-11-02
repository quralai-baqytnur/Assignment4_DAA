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
import report.CsvReport;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class MainGraphRunner {

    static class MetricMap implements Metrics {
        private final Map<String, Long> map = new HashMap<>();
        public void inc(String k, long d) { map.merge(k, d, Long::sum); }
        public long get(String k) { return map.getOrDefault(k, 0L); }
    }

    private static final List<String> CSV_HEADERS = List.of(
            "file","n","m",
            "scc_count","scc_sizes",
            "cond_nodes","cond_edges","cond_topo_len","pushes","pops",
            "is_dag_file","source",
            "sp_dist_last","sp_path_last",
            "lp_target","lp_len","lp_path",
            "relax_sp","relax_lp","time_sp_ns","time_longest_ns"
    );

    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");
        List<Path> files;
        try (var s = Files.list(dataDir)) {
            files = s.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }

        JSONArray results = new JSONArray();

        Path outDir = Path.of("out");
        Files.createDirectories(outDir);
        Path csvPath = outDir.resolve("metrics.csv");

        try (CsvReport csv = new CsvReport(csvPath, CSV_HEADERS, true)) {
            csv.open();

            for (Path p : files) {
                JSONObject one = processFileToJson(p);
                results.put(one);

                Map<String, Object> row = jsonToCsvRow(one);
                csv.append(row);
            }
        }

        Files.writeString(Path.of("results.json"), results.toString(2));
        System.out.println("JSON report saved to results.json");
        System.out.println("CSV  report saved to summary.csv");
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

                JSONArray distJson = new JSONArray();
                for (long d : spRes.dist) distJson.put(d == Long.MAX_VALUE ? "INF" : d);

                JSONObject shortest = new JSONObject();
                shortest.put("distances", distJson);
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

    private static Map<String, Object> jsonToCsvRow(JSONObject one) {
        Map<String, Object> row = new LinkedHashMap<>();

        String file = one.optString("file", "");
        int n = one.optInt("n", 0);
        int m = one.optInt("m", 0);

        JSONObject scc = one.optJSONObject("scc");
        int sccCount = (scc != null) ? scc.optInt("count", 0) : 0;
        List<Object> sccSizes = jsonArrayToList(scc != null ? scc.optJSONArray("sizes") : null);

        JSONObject condensation = one.optJSONObject("condensation");
        int condNodes = (condensation != null) ? condensation.optInt("dag_nodes", 0) : 0;
        int condEdges = (condensation != null) ? condensation.optInt("dag_edges", 0) : 0;
        int condTopoLen = (condensation != null && condensation.has("topo_order"))
                ? condensation.getJSONArray("topo_order").length() : 0;
        long pushes = (condensation != null) ? condensation.optLong("pushes", 0) : 0;
        long pops   = (condensation != null) ? condensation.optLong("pops", 0) : 0;

        boolean isDagFile = one.has("dag");
        Integer source = null;
        Object spDistLast = "";
        List<Object> spPathLast = List.of();
        Object lpTarget = "";
        Object lpLen = "";
        List<Object> lpPath = List.of();
        Long relaxSp = null, relaxLp = null, timeSp = null, timeLp = null;

        if (isDagFile) {
            JSONObject dag = one.getJSONObject("dag");
            source = dag.optInt("source", 0);

            JSONObject shortest = dag.optJSONObject("shortest");
            if (shortest != null) {
                JSONArray distances = shortest.optJSONArray("distances");
                int tSp = one.optInt("n", 1) - 1; // last vertex
                if (distances != null && tSp >= 0 && tSp < distances.length()) {
                    spDistLast = distances.get(tSp); // может быть "INF" или число
                }
                spPathLast = jsonArrayToList(shortest.optJSONArray("example_path"));
            }

            JSONObject longest = dag.optJSONObject("longest");
            if (longest != null) {
                lpTarget = longest.opt("argmax_target");
                Object length = longest.opt("length");
                lpLen = (length == JSONObject.NULL) ? "N/A" : length;
                lpPath = jsonArrayToList(longest.optJSONArray("path"));
            }

            JSONObject met = dag.optJSONObject("metrics");
            if (met != null) {
                relaxSp = met.optLong("relax_sp", 0);
                relaxLp = met.optLong("relax_lp", 0);
                timeSp = met.optLong("time_sp_ns", 0);
                timeLp = met.optLong("time_longest_ns", 0);
            }
        }

        row.put("file", file);
        row.put("n", n);
        row.put("m", m);

        row.put("scc_count", sccCount);
        row.put("scc_sizes", sccSizes);

        row.put("cond_nodes", condNodes);
        row.put("cond_edges", condEdges);
        row.put("cond_topo_len", condTopoLen);
        row.put("pushes", pushes);
        row.put("pops", pops);

        row.put("is_dag_file", isDagFile);
        row.put("source", source == null ? "" : source);

        row.put("sp_dist_last", spDistLast);
        row.put("sp_path_last", spPathLast);

        row.put("lp_target", lpTarget);
        row.put("lp_len", lpLen);
        row.put("lp_path", lpPath);

        row.put("relax_sp", relaxSp == null ? "" : relaxSp);
        row.put("relax_lp", relaxLp == null ? "" : relaxLp);
        row.put("time_sp_ns", timeSp == null ? "" : timeSp);
        row.put("time_longest_ns", timeLp == null ? "" : timeLp);

        return row;
    }

    private static List<Object> jsonArrayToList(JSONArray arr) {
        if (arr == null) return List.of();
        List<Object> list = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            Object val = arr.get(i);
            list.add(val == JSONObject.NULL ? null : val);
        }
        return list;
    }
}
