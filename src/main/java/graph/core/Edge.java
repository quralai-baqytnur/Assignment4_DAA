package graph.core;

public class Edge {
    public final int u;
    public final int v;
    public final int w;

    public Edge(int u, int v, int w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    @Override
    public String toString() {
        return "(" + u + "->" + v + ", w=" + w + ")";
    }
}
