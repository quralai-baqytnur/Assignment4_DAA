package utils;

import graph.core.Edge;
import graph.core.Graph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class GraphReader {

    public static Graph fromJsonFile(Path path) throws IOException {
        String text = Files.readString(path);
        JSONObject root = new JSONObject(text);

        boolean directed = root.getBoolean("directed");
        int n = root.getInt("n");
        String weightModel = root.optString("weight_model", "edge");
        Integer source = root.has("source") ? root.getInt("source") : null;

        JSONArray edgesArr = root.getJSONArray("edges");
        var edges = new ArrayList<Edge>(edgesArr.length());
        for (int i = 0; i < edgesArr.length(); i++) {
            JSONObject e = edgesArr.getJSONObject(i);
            int u = e.getInt("u");
            int v = e.getInt("v");
            int w = e.getInt("w");
            edges.add(new Edge(u, v, w));
        }

        return new Graph(directed, n, edges, source, weightModel);
    }
}
