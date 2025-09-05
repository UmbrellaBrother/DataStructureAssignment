import java.util.*;

public class UnweightedGraph<V> extends AbstractGraph<V> {

    public UnweightedGraph(V[] vertices, int[][] edges) {
        super(vertices, edges);
    }

    @Override
    public Tree dfs(int v) {
        List<Integer> searchOrder = new ArrayList<>();
        int[] parent = new int[getSize()];
        Arrays.fill(parent, -1);

        boolean[] visited = new boolean[getSize()];
        dfs(v, parent, searchOrder, visited);

        return new Tree(v, parent, searchOrder);
    }

    private void dfs(int u, int[] parent, List<Integer> searchOrder, boolean[] visited) {
        visited[u] = true;
        searchOrder.add(u);

        for (int w : getNeighbours(u)) {
            if (!visited[w]) {
                parent[w] = u;
                dfs(w, parent, searchOrder, visited);
            }
        }
    }

    @Override
    public Tree bfs(int v) {
        List<Integer> searchOrder = new ArrayList<>();
        int[] parent = new int[getSize()];
        Arrays.fill(parent, -1);

        boolean[] visited = new boolean[getSize()];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(v);
        visited[v] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            searchOrder.add(u);

            for (int w : getNeighbours(u)) {
                if (!visited[w]) {
                    queue.offer(w);
                    parent[w] = u;
                    visited[w] = true;
                }
            }
        }

        return new Tree(v, parent, searchOrder);
    }
}
