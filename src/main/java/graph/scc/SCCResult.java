package graph.scc;

import java.util.*;

public class SCCResult {
    private final List<List<Integer>> components;
    private final int count;

    public SCCResult(List<List<Integer>> components) {
        this.components = components;
        this.count = components.size();
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "SCC count=" + count + ", components=" + components;
    }
}
