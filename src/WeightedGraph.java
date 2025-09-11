import java.util.*;

public class WeightedGraph<V> {
    private final List<V> vertices = new ArrayList<>();
    private final List<List<Edge>> neighbors = new ArrayList<>();

    public WeightedGraph(V[] vertices) {
        Collections.addAll(this.vertices, vertices);
        for (int i = 0; i < vertices.length; i++) {
            neighbors.add(new ArrayList<>());
        }
    }

    public int getSize() {
        return vertices.size();
    }

    public int getIndex(V v) {
        return vertices.indexOf(v);
    }

    public V getVertex(int index) {
        return vertices.get(index);
    }

    public void addEdge(int from, int to, double time, double cost) {
        neighbors.get(from).add(new Edge(to, time, cost));
    }

    public List<Edge> getEdges(int from) {
        return neighbors.get(from);
    }

    public static class Edge {
        public int to;
        public double time;
        public double cost;

        public Edge(int to, double time, double cost) {
            this.to = to;
            this.time = time;
            this.cost = cost;
        }
    }
}
