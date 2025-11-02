package graph.core;

import org.junit.jupiter.api.Test;
import utils.GraphReader;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GraphReaderTest {

    @Test
    void loadsSmallDag() throws Exception {
        Graph g = GraphReader.fromJsonFile(Path.of("data/small_dag.json"));
        assertTrue(g.isDirected());
        assertEquals(6, g.n());
        assertFalse(g.edges().isEmpty());
        assertEquals("edge", g.weightModel());
        assertNotNull(g.source());
        int totalOut = g.adjOut().stream().mapToInt(java.util.List::size).sum();
        assertEquals(g.edges().size(), totalOut);
    }
}
