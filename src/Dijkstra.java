import java.util.*;

public class Dijkstra<V> {
    private final WeightedGraph<V> graph;

    public Dijkstra(WeightedGraph<V> graph) {
        this.graph = graph;
    }

    // mode = "time" for shortest travel time, "cost" for cheapest route
    public List<V> getPath(V start, V end, String mode) {
        int n = graph.getSize();
        int startIndex = graph.getIndex(start);
        int endIndex = graph.getIndex(end);

        if (startIndex == -1 || endIndex == -1) return Collections.emptyList();

        double[] dist = new double[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);

        dist[startIndex] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> dist[a[0]]));
        pq.add(new int[]{startIndex});

        while (!pq.isEmpty()) {
            int u = pq.poll()[0];

            for (WeightedGraph.Edge e : graph.getEdges(u)) {
                int v = e.to;
                double weight = mode.equals("time") ? e.time : e.cost; // choose metric
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    pq.add(new int[]{v, (int) dist[v]});
                }
            }
        }

        // reconstruct path
        List<V> path = new ArrayList<>();
        for (int at = endIndex; at != -1; at = prev[at]) {
            path.add(graph.getVertex(at));
        }
        Collections.reverse(path);
        return path;
    }
}
