package graph.scc;

import graph.core.Graph;
import graph.core.Edge;
import java.util.*;

public class CondensationGraph {

    private final Graph original;
    private final SCCResult sccResult;

    public CondensationGraph(Graph original, SCCResult sccResult) {
        this.original = original;
        this.sccResult = sccResult;
    }

    public Graph buildCondensedGraph() {
        int sccCount = sccResult.getCount();
        Map<Integer, Integer> vertexToSCC = new HashMap<>();

        for (int i = 0; i < sccCount; i++) {
            for (int v : sccResult.getComponents().get(i)) {
                vertexToSCC.put(v, i);
            }
        }

        Set<String> seen = new HashSet<>();
        List<Edge> dagEdges = new ArrayList<>();

        for (int u = 0; u < original.n(); u++) {
            for (int[] vw : original.adjOut().get(u)) {
                int v = vw[0];
                int sccU = vertexToSCC.get(u);
                int sccV = vertexToSCC.get(v);
                if (sccU != sccV) {
                    String key = sccU + "->" + sccV;
                    if (seen.add(key)) {
                        dagEdges.add(new Edge(sccU, sccV, 1));
                    }
                }
            }
        }

        return new Graph(true, sccCount, dagEdges, null, "unit");
    }

    public void printCondensationSummary(Graph dag) {
        System.out.println("Condensation graph has " + dag.n() + " nodes (SCCs).");
        for (int i = 0; i < sccResult.getCount(); i++) {
            System.out.println("SCC " + i + ": " + sccResult.getComponents().get(i));
        }
        System.out.println("DAG edges:");
        for (Edge e : dag.edges()) {
            System.out.println("  " + e.u + " -> " + e.v);
        }
    }
}
