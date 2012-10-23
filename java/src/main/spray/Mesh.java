package spray;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import spray.Geometry.Line2;
import spray.Geometry.Side;
import spray.Geometry.Vec2;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.min;
import static java.util.Collections.unmodifiableCollection;
import static spray.Geometry.*;
import static org.testng.collections.Lists.newArrayList;
import static org.testng.collections.Maps.newHashMap;

public final class Mesh {

    int previousVertexId, previousTriangleId;

    List<Triangle> triangles = newArrayList();

    public Collection<Triangle> triangles() {
        return unmodifiableCollection(triangles);
    }

    List<Vertex> vertices = newArrayList();

    public Collection<Vertex> vertices() {
        return unmodifiableCollection(vertices);
    }

    public Mesh() {
    }

    public Mesh(Collection<VertexConfig> points) {
        setPoints(points);
    }

    private boolean meshIsValid() {
        for (Vertex v : vertices) {
            assert v.corner.vertex == v;
            Lists.<Corner>newArrayList(v.corners());
        }
        for (Triangle t : triangles) {
            for (Corner c : t.corners()) {
                assert vertices.contains(c.vertex);
            }
        }
        return true;
    }

    public void setPoints(Collection<VertexConfig> points) {
        Delaunay d = new Delaunay(points);
        triangles = d.triangles;
        vertices = d.vertices;
    }

    public Collection<Edge> edges() {
        Set<Edge> edges = newHashSet();
        for (Triangle t : triangles) edges.addAll(t.edges());
        return edges;
    }

    boolean exists(Edge e) {
        return vertices.contains(e.a) & vertices.contains(e.b);
    }

    public static class VertexConfig {
        Vec2 loc;

        public VertexConfig(Vec2 loc) {
            this.loc = loc;
        }
    }

    public class Vertex {
        private final int id = ++previousVertexId;

        public int id() {
            return id;
        }

        public int hashCode() {
            return id;
        }

        private Vec2 loc;

        public Vec2 loc() {
            return loc;
        }

        private Vertex(VertexConfig config) {
            this.loc = config.loc;
        }

        private Corner corner;

        public Corner corner() {
            return corner;
        }

        public Iterable<Corner> corners() {
            return new Iterable<Corner>() {
                public Iterator<Corner> iterator() {
                    return cornersIter();
                }
            };
        }

        public Iterator<Corner> cornersIter() {
            return new Iterator<Corner>() {
                Corner c = Vertex.this.corner;
                Set<Corner> visited; {
                    assert (visited = newHashSet()) != null;
                }

                public boolean hasNext() {
                    return c != null;
                }

                public Corner next() {
                    if (c == null) throw new NoSuchElementException();
                    Corner retval = c;
                    c = c.swings.next.corner;
                    if (c == Vertex.this.corner) c = null;
                    assert visited.add(retval);
                    assert triangles.contains(retval.triangle);
                    return retval;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public class Corner {
        private Triangle triangle;
        private Corner next, prev;
        private Vertex vertex;
        private Swings swings = new Swings();

        private Corner(Vertex vertex, Triangle triangle) {
            this.vertex = vertex;
            this.triangle = triangle;
            if (vertex.corner == null) vertex.corner = this;
        }

        public Triangle triangle() {
            return triangle;
        }

        public Vertex vertex() {
            return vertex;
        }

        public Corner next() {
            return next;
        }

        public Corner prev() {
            return prev;
        }

        public Swings swing() {
            return swings;
        }
    }

    public static class Swings {
        private Swing prev = new Swing(), next = new Swing();

        public Swing prev() {
            return prev;
        }

        public Swing next() {
            return next;
        }
    }

    public static class Swing {
        private Corner corner;

        public Corner corner() {
            return corner;
        }

        private boolean isSuper;

        public boolean isSuper() {
            return isSuper;
        }

        public Swing() {
        }

        public Swing(Corner corner, boolean isSuper) {
            this.corner = corner;
            this.isSuper = isSuper;
        }

        public Swing copy() {
            Swing x = new Swing();
            x.corner = corner;
            x.isSuper = isSuper;
            return x;
        }
    }

    private static void setSwing(Corner prev, Corner next) {
        prev.swings.next.corner = next;
        next.swings.prev.corner = prev;
    }

    private static void setSwing(Corner prev, Corner next, boolean isSuper) {
        setSwing(prev, next);
        prev.swings.next.isSuper = isSuper;
        next.swings.prev.isSuper = isSuper;
    }

    private static void setSwing(Corner prev, boolean isSuper) {
        setSwing(prev, prev.swings.next.corner, isSuper);
    }

    public class Edge {
        private final Vertex a, b;

        private Edge(Vertex a, Vertex b) {
            boolean flip = a.id > b.id;
            this.a = flip ? b : a;
            this.b = flip ? a : b;
        }

        public Vertex a() {
            return a;
        }

        public Vertex b() {
            return b;
        }

        public List<Vertex> vertices() {
            return asList(a(), b());
        }

        public Line2 line() {
            return aToB(a.loc, b.loc);
        }

        public boolean equals(Object o) {
            return this == o || (o instanceof Edge && a == ((Edge) o).a && b == ((Edge) o).b);
        }

        public int hashCode() {
            return 31 * a.hashCode() + b.hashCode();
        }

        public List<Triangle> triangles() {
            List<Triangle> ts = newArrayList(2);
            Corner c1 = null;
            for (Corner c : a.corners()) if (c.next.vertex == b) c1 = c;
            if (c1 != null) ts.add(c1.triangle);
            Corner c2 = null;
            for (Corner c : b.corners()) if (c.next.vertex == a) c2 = c;
            if (c2 != null) ts.add(c2.triangle);
            return ts;
        }
    }

    public class Triangle {
        private final int id = ++previousTriangleId;

        public int id() {
            return id;
        }

        public int hashCode() {
            return id;
        }

        private final Corner a, b, c;

        public Triangle(Vertex a, Vertex b, Vertex c) {
            this.a = new Corner(a, this);
            this.b = new Corner(b, this);
            this.c = new Corner(c, this);
            initNextPrev();
        }

        public Corner a() {
            return a;
        }

        public Corner b() {
            return b;
        }

        public Corner c() {
            return c;
        }

        private void initNextPrev() {
            a.next = b;
            b.next = c;
            c.next = a;
            a.prev = c;
            b.prev = a;
            c.prev = b;
        }

        public List<Corner> corners() {
            return asList(a, b, c);
        }

        public List<Edge> edges() {
            Vertex a = this.a.vertex, b = this.b.vertex, c = this.c.vertex;
            return asList(new Edge(a, b), new Edge(b, c), new Edge(c, a));
        }

        public List<Line2> lines() {
            Vec2 a = this.a.vertex.loc, b = this.b.vertex.loc, c = this.c.vertex.loc;
            return asList(aToB(a, b), aToB(b, c), aToB(c, a));
        }

        public boolean contains(Vec2 p) {
            for (Line2 l : lines()) if (l.side(p) != Side.LEFT) return false;
            return true;
        }

        public Corner earCorner() {
            for (Corner corner : asList(a, b, c)) if (corner.swings.next.corner == corner) return corner;
            return null;
        }

        public Corner corner(Vertex v) {
            for (Corner x : corners()) if (x.vertex == v) return x;
            return null;
        }
    }

    private class Delaunay {

        List<Triangle> triangles = newArrayList();
        List<Vertex> vertices = newArrayList();
        List<Edge> edges = newArrayList();
        List<Edge> convexHull = newArrayList();
        Map<Edge, Vertex> openEdges = newHashMap();

        Delaunay(Collection<VertexConfig> points) {
            if (points.size() < 3) throw new IllegalArgumentException();
            for (VertexConfig p : points) vertices.add(new Vertex(p));
            calculateConvexHull();
            for (Edge edge : convexHull.subList(0, 1)) {
                edges.add(edge);
                openEdges.put(edge, null);
            }
            while (openEdges.size() != 0) tryNextEdge();
            calculateSwing();
        }

        void calculateConvexHull() {
            final Vertex start = min(vertices, new Comparator<Vertex>() {
                public int compare(Vertex i, Vertex j) {
                    return Float.compare(key(i), key(j));
                }

                float key(Vertex v) {
                    return v.loc().y();
                }
            });
            Vertex a = start;
            while (true) {
                final Vertex a$ = a;
                Vertex b = min(vertices, new Comparator<Vertex>() {
                    public int compare(Vertex i, Vertex j) {
                        return Float.compare(key(i), key(j));
                    }

                    float key(Vertex v) {
                        return v == a$ ? Float.MAX_VALUE : (v.loc().sub(a$.loc())).ang();
                    }
                });
                convexHull.add(new Edge(a, b));
                a = b;
                if (a == start) break;
            }
        }

        void tryNextEdge() {
            final Edge edge;
            Vertex previousVertex;
            {
                Entry<Edge, Vertex> entry = openEdges.entrySet().iterator().next();
                edge = entry.getKey();
                previousVertex = entry.getValue();
                openEdges.remove(edge);
            }
            final Line2 line = edge.line();
            Iterable<Vertex> candidateVertices;
            if (previousVertex == null) {
                candidateVertices = Iterables.filter(vertices, new Predicate<Vertex>() {
                    public boolean apply(Vertex vertex) {
                        return !edge.vertices().contains(vertex);
                    }
                });
            } else {
                if (convexHull.contains(edge)) return;
                final Side side = line.side(previousVertex.loc()).opposite();
                candidateVertices = Iterables.filter(vertices, new Predicate<Vertex>() {
                    public boolean apply(Vertex vertex) {
                        return !edge.vertices().contains(vertex) && line.side(vertex.loc()) == side;
                    }
                });
            }
            if (!candidateVertices.iterator().hasNext()) return;
            final Vertex v = Ordering.natural().onResultOf(new Function<Vertex, Float>() {
                public Float apply(Vertex v) {
                    return line.bulge(v.loc());
                }
            }).min(candidateVertices);
            Triangle t;
            {
                Vertex[] tv = {edge.a(), edge.b(), v};
                // vertices are sorted in clockwise rotation about the circumcenter
                final Vec2 cc = circle(tv[0].loc(), tv[1].loc(), tv[2].loc()).center();
                class X {
                    final float ang;
                    final Vertex v;

                    X(Vertex v) {
                        this.v = v;
                        ang = v.loc().sub(cc).ang();
                    }
                }
                X[] xs = {new X(tv[0]), new X(tv[1]), new X(tv[2])};
                Arrays.sort(xs, new Comparator<X>() {
                    public int compare(X a, X b) {
                        return Float.compare(a.ang, b.ang);
                    }
                });
                t = new Triangle(xs[0].v, xs[1].v, xs[2].v);
            }
            triangles.add(t);
            for (List<Vertex> vertexPair : ImmutableList.of(edge.vertices(), Lists.reverse(edge.vertices()))) {
                Vertex u = vertexPair.get(0), w = vertexPair.get(1);
                Edge uv = new Edge(u, v);
                if (openEdges.remove(uv) == null) {
                    edges.add(uv);
                    openEdges.put(uv, w);
                }
            }
        }

        void calculateSwing() {
            Multimap<Vertex, Corner> v2c = ArrayListMultimap.create();
            for (Triangle t : triangles) for (Corner c : t.corners()) v2c.put(c.vertex, c);
            for (Collection<Corner> cs : v2c.asMap().values()) {
                for (Corner i : cs) for (Corner j : cs) if (i.next.vertex == j.prev.vertex) setSwing(j, i);
                Corner si = null, sj = null;
                for (Corner i : cs) {
                    if (i.swings.next.corner == null) si = i;
                    if (i.swings.prev.corner == null) sj = i;
                }
                if (si != null) setSwing(si, sj, true);
            }
        }

    }

}
