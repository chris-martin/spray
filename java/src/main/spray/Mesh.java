package spray;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import spray.Geometry.IsVec3;
import spray.Geometry.Line3;
import spray.Geometry.Side;
import spray.Geometry.Vec2;
import spray.Geometry.Vec3;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.testng.collections.Lists.newArrayList;
import static spray.Geometry.aToB;
import static spray.Geometry.distance;
import static spray.Geometry.sphere;

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

    public Mesh(Balls<?> balls) {
        setBalls(balls);
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

    public void setBalls(Balls<?> balls) {

        Balls<Vertex> vBalls = new Balls<Vertex>(
            FluentIterable.from(balls.balls).transform(new Function<IsVec3, Vertex>() {
                public Vertex apply(IsVec3 point) {
                    return new Vertex(point.asVec3());
                }
            })
        );

        while (vertices.size() != 0) {
            Builder builder = new Builder(vBalls);
            triangles.addAll(builder.triangles);
            for (Vertex v : builder.vertices) {
                vBalls.balls.remove(v);
            }
        }

    }

    public Collection<Edge> edges() {
        Set<Edge> edges = newHashSet();
        for (Triangle t : triangles) edges.addAll(t.edges());
        return edges;
    }

    boolean exists(Edge e) {
        return vertices.contains(e.a) & vertices.contains(e.b);
    }

    public class Vertex implements IsVec3 {

        private final int id = ++previousVertexId;

        public int id() {
            return id;
        }

        public int hashCode() {
            return id;
        }

        private Vec3 loc;

        public Vec3 asVec3() {
            return loc;
        }

        private Vertex(Vec3 loc) {
            this.loc = loc;
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

        public Line3 line() {
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

    public class DirectedEdge {

        private final Vertex a, b;

        private DirectedEdge(Vertex a, Vertex b) {
            this.a = a;
            this.b = b;
        }

        public Vertex a() {
            return a;
        }

        public Vertex b() {
            return b;
        }

        public boolean equals(Object o) {
            return this == o || (o instanceof Edge && a == ((Edge) o).a && b == ((Edge) o).b);
        }

        public int hashCode() {
            return 31 * a.hashCode() + b.hashCode();
        }

        public Line3 line() {
            return aToB(a.loc, b.loc);
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

        public FluentIterable<Vertex> vertices() {
            return FluentIterable.from(corners())
                .transform(new Function<Corner, Vertex>() {
                    public Vertex apply(Corner corner) {
                        return corner.vertex;
                    }
                });
        }

        public List<Edge> edges() {
            Vertex a = this.a.vertex, b = this.b.vertex, c = this.c.vertex;
            return asList(new Edge(a, b), new Edge(b, c), new Edge(c, a));
        }

        public List<Line3> lines() {
            Vec3 a = this.a.vertex.loc, b = this.b.vertex.loc, c = this.c.vertex.loc;
            return asList(aToB(a, b), aToB(b, c), aToB(c, a));
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

    private class OpenEdge {

        final DirectedEdge edge;
        final Vec3 rollFrom;

        private OpenEdge(DirectedEdge edge, Vec3 rollFrom) {
            this.edge = edge;
            this.rollFrom = rollFrom;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OpenEdge openEdge = (OpenEdge) o;
            return edge.equals(openEdge.edge);
        }

        public int hashCode() {
            return edge != null ? edge.hashCode() : 0;
        }
    }

    private static class RollingCollision {

        final Vec3 rolling;
        final Vertex vertex;

        private RollingCollision(Vec3 rolling, Vertex vertex) {
            this.rolling = rolling;
            this.vertex = vertex;
        }

    }

    private static <T> Predicate<T> sameAs(final T t) {
        return new Predicate<T>() {
            public boolean apply(T s) {
                return t == s;
            }
        };
    }

    private static <V extends IsVec3> Predicate<V> ballsAreTouching(final IsVec3 p, final float radius) {
        return new Predicate<V>() {
            public boolean apply(V q) {
                return distance(p, q) < 2 * radius;
            }
        };
    }

    private static Function<Vertex, RollingCollision> vertexCollisionWith(final Vec3 rolling) {
        return new Function<Vertex, RollingCollision>() {
            public RollingCollision apply(Vertex vertex) {
                return new RollingCollision(rolling, vertex);
            }
        };
    }

    private class Builder {

        final Balls<Vertex> balls;
        List<Triangle> triangles = newArrayList();
        List<Vertex> vertices = newArrayList();
        Set<OpenEdge> openEdges = newHashSet();

        Builder(final Balls<Vertex> balls) {

            this.balls = balls;

            final Vertex a = Ordering.natural().onResultOf(new Function<Vertex, Float>() {
                public Float apply(Vertex vertex) {
                    return vertex.loc.z();
                }
            }).min(balls.balls);

            vertices.add(a);

            final RollingCollision collision = Approximation
                .samplePointsAroundSphere(sphere(a, 2 * balls.radius))
                .transformAndConcat(new Function<Vec3, Iterable<RollingCollision>>() {
                    public Iterable<RollingCollision> apply(final Vec3 rolling) {
                        return FluentIterable
                            .from(balls.balls)
                            .filter(Predicates.not(sameAs(a)))
                            .filter(ballsAreTouching(rolling, balls.radius))
                            .transform(vertexCollisionWith(rolling));
                    }
                })
                .first()
                .orNull();

            if (collision == null) {
                return;
            }

            final Vec3 rolling = collision.rolling;
            final Vertex b = collision.vertex;

            vertices.add(b);
            openEdges.add(new OpenEdge(new DirectedEdge(a, b), rolling));
            openEdges.add(new OpenEdge(new DirectedEdge(b, a), rolling));

            while (openEdges.size() != 0) {
                tryNextEdge();
            }
        }

        void tryNextEdge() {

            final OpenEdge openEdge = openEdges.iterator().next();
            openEdges.remove(openEdge);

            final RollingCollision collision = Approximation
                .samplePointsAroundLine(openEdge.edge.line(), openEdge.rollFrom)
                .transformAndConcat(new Function<Vec3, Iterable<RollingCollision>>() {
                    public Iterable<RollingCollision> apply(final Vec3 rolling) {
                        return FluentIterable
                            .from(balls.balls)
                            .filter(Predicates.not(sameAs(openEdge.edge.a)))
                            .filter(Predicates.not(sameAs(openEdge.edge.b)))
                            .transform(vertexCollisionWith(rolling));
                    }
                })
                .first()
                .orNull();



            Iterable<Vertex> candidateVertices;
            if (previousVertex == null) {
                candidateVertices = Iterables.filter(vertices, new Predicate<Vertex>() {
                    public boolean apply(Vertex vertex) {
                        return !edge.vertices().contains(vertex);
                    }
                });
            } else {
                if (convexHull.contains(edge)) return;
                final Side side = line.side(previousVertex.asVec3()).opposite();
                candidateVertices = Iterables.filter(vertices, new Predicate<Vertex>() {
                    public boolean apply(Vertex vertex) {
                        return !edge.vertices().contains(vertex) && line.side(vertex.asVec3()) == side;
                    }
                });
            }
            if (!candidateVertices.iterator().hasNext()) return;
            final Vertex v = Ordering.natural().onResultOf(new Function<Vertex, Float>() {
                public Float apply(Vertex v) {
                    return line.bulge(v.asVec3());
                }
            }).min(candidateVertices);
            Triangle t;
            {
                Vertex[] tv = {edge.a(), edge.b(), v};
                // vertices are sorted in clockwise rotation about the circumcenter
                final Vec2 cc = circle(tv[0].asVec3(), tv[1].asVec3(), tv[2].asVec3()).center();
                class X {
                    final float ang;
                    final Vertex v;

                    X(Vertex v) {
                        this.v = v;
                        ang = v.asVec3().sub(cc).ang();
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

    }

}
