package graph.core;
import java.util.*;

public class Graph {
    private final boolean directed;
    private final int n;
    private final List<Edge> edges;
    private final List<List<int[]>> adjOut;
    private final List<List<int[]>> adjIn;
    private final Integer source;
    private final String weightModel;

    public Graph(boolean directed, int n, List<Edge> edges, Integer source, String weightModel) {
        this.directed = directed;
        this.n = n;
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
        this.source = source;
        this.weightModel = weightModel;

        List<List<int[]>> out = new ArrayList<>(n);
        List<List<int[]>> in  = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(new ArrayList<>());
            in.add(new ArrayList<>());
        }
        for (Edge e : edges) {
            out.get(e.u).add(new int[]{e.v, e.w});
            in.get(e.v).add(new int[]{e.u, e.w});
            if (!directed) {
                out.get(e.v).add(new int[]{e.u, e.w});
                in.get(e.u).add(new int[]{e.v, e.w});
            }
        }
        this.adjOut = Collections.unmodifiableList(out);
        this.adjIn  = Collections.unmodifiableList(in);
    }

    public boolean isDirected() { return directed; }
    public int n() { return n; }
    public List<Edge> edges() { return edges; }
    public List<List<int[]>> adjOut() { return adjOut; }
    public List<List<int[]>> adjIn() { return adjIn; }
    public Integer source() { return source; }
    public String weightModel() { return weightModel; }

    public int outDegree(int u) { return adjOut.get(u).size(); }
    public int inDegree(int v)  { return adjIn.get(v).size();  }
}
