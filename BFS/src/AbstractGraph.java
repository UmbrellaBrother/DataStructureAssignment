import java.util.*;

public abstract class AbstractGraph<V> implements Graph<V> {
    protected List<V> vertices = new ArrayList<>();
    protected List<List<Integer>> neighbours = new ArrayList<>();

    public AbstractGraph(V[] vertices, int[][] edges) {
        for (V v : vertices) {
            this.vertices.add(v);
            neighbours.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            addEdge(e[0], e[1]);
        }
    }

    @Override
    public int getSize() {
        return vertices.size();
    }
    
    @Override
    public List<V> getVertices() {
        return vertices; 
    }

    @Override
    public V getVertex(int index) {
        return vertices.get(index);
    }

    @Override
    public int getIndex(V v) {
        return vertices.indexOf(v);
    }

    @Override
    public List<Integer> getNeighbours(int index) {
        return neighbours.get(index);
    }

    @Override
    public int getDegree(int index) {
        return neighbours.get(index).size();
    }

    @Override
    public void printEdges() {
        for (int i = 0; i < neighbours.size(); i++) {
            System.out.print(vertices.get(i) + " -> ");
            for (int j : neighbours.get(i)) {
                System.out.print(vertices.get(j) + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void clear() {
        vertices.clear();
        neighbours.clear();
    }

    @Override
    public boolean addVertex(V v) {
        if (!vertices.contains(v)) {
            vertices.add(v);
            neighbours.add(new ArrayList<>());
            return true;
        }
        return false;
    }

    @Override
    public boolean addEdge(int u, int v) {
        if (u < 0 || u >= getSize() || v < 0 || v >= getSize()) return false;
        if (!neighbours.get(u).contains(v)) {
            neighbours.get(u).add(v);
            return true;
        }
        return false;
    }

    public class Tree {
        private int root;
        private int[] parent;
        private List<Integer> searchOrder = new ArrayList<>();

        public Tree(int root, int[] parent, List<Integer> searchOrder) {
            this.root = root;
            this.parent = parent;
            this.searchOrder = searchOrder;
        }

        public int getRoot() {
            return root;
        }

        public int getParent(int v) {
            return parent[v];
        }

        public List<Integer> getSearchOrder() {
            return searchOrder;
        }
    }

    // ---------- Abstract DFS / BFS ----------
    @Override
    public abstract Tree dfs(int v);

    @Override
    public abstract Tree bfs(int v);
}
