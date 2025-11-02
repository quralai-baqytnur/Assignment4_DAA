package utils;


public interface Metrics {
    void inc(String key, long delta);
    long get(String key);
}
