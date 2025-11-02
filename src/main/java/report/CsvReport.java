package report;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CsvReport implements AutoCloseable {
    private final Path path;
    private final List<String> headers;
    private final boolean overwrite;
    private BufferedWriter out;
    private boolean headerWritten = false;

    public CsvReport(Path path, List<String> headers, boolean overwrite) {
        this.path = path;
        this.headers = new ArrayList<>(headers);
        this.overwrite = overwrite;
    }

    public void open() throws IOException {
        if (out != null) return;
        if (overwrite) {
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());
        }
        out = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        writeHeaderIfNeeded();
    }

    private void writeHeaderIfNeeded() throws IOException {
        if (!headerWritten) {
            writeLine(headers);
            headerWritten = true;
        }
    }

    public void append(Map<String, ?> row) throws IOException {
        if (out == null) open();
        List<String> values = new ArrayList<>(headers.size());
        for (String h : headers) {
            Object v = row.get(h);
            values.add(serialize(v));
        }
        writeLine(values);
    }

    private void writeLine(List<String> cells) throws IOException {
        String line = String.join(",", cells);
        out.write(line);
        out.newLine();
        out.flush();
    }

    private String serialize(Object v) {
        if (v == null) return "";
        if (v instanceof Number || v instanceof Boolean) return v.toString();

        if (v instanceof Collection<?> col) {
            return quote("[" + joinCollection(col) + "]");
        }
        if (v.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(v);
            List<String> items = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object el = java.lang.reflect.Array.get(v, i);
                items.add(String.valueOf(el));
            }
            return quote("[" + String.join(", ", items) + "]");
        }

        return quote(v.toString());
    }

    private String joinCollection(Collection<?> col) {
        List<String> parts = new ArrayList<>(col.size());
        for (Object o : col) parts.add(String.valueOf(o));
        return String.join(", ", parts);
    }

    private String quote(String s) {
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String esc = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + esc + "\"" : esc;
    }

    @Override
    public void close() throws IOException {
        if (out != null) out.close();
    }
}
