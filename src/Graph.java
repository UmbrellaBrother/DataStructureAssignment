import java.util.*;

public interface Graph<V> {
    public int getSize();
    public List<V> getVertices();
    public V getVertex(int index);
    public int getIndex(V v);
    public List<Integer> getNeighbours(int index);
    public int getDegree(int index);
    public void printEdges();
    public void clear();
    public boolean addVertex(V v);
    
    public boolean addEdge(int u, int v);
    
    AbstractGraph<V>.Tree dfs(int v);
    AbstractGraph<V>.Tree bfs(int v);
    
}
