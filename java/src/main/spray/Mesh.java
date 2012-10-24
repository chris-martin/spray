package spray;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import java.util.*;
import java.util.Map.Entry;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.testng.collections.Lists.newArrayList;
import static spray.Geometry.*;
import static spray.Geometry.distance;

public final class Mesh {

    int previousVertexId, previousTriangleId;

    List<Triangle> triangles = newArrayList();

    public List<Triangle> triangles() {
        return unmodifiableList(triangles);
    }

    float rollingScale = 4;

    public Mesh() { }

    public Mesh(Balls<?> balls) {
        setBalls(balls);
    }

    public void setBalls(Balls<?> balls) {

        Balls<Vertex> vBalls = new Balls<Vertex>(
            FluentIterable.from(balls.balls).transform(new Function<IsVec3, Vertex>() {
                public Vertex apply(IsVec3 point) {
                    return new Vertex(point.asVec3());
                }
            })
        );

        int i = 0;
        while (vBalls.balls.size() != 0) {
            i++;
            Builder builder = new Builder(vBalls);
            triangles.addAll(builder.triangles);
            for (Vertex v : builder.vertices) {
                vBalls.balls.remove(v);
            }
        }
        //System.out.println(i + " components");

    }

    public Collection<Edge> edges() {
        Set<Edge> edges = newHashSet();
        for (Triangle t : triangles) edges.addAll(t.edges());
        return edges;
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

    /**
     * Edges are undirected.
     */
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

    public class Triangle {
        private final int id = ++previousTriangleId;

        public int hashCode() {
            return id;
        }

        private final Corner a, b, c;

        public Triangle(Vertex a, Vertex b, Vertex c) {
            this.a = new Corner(a, this);
            this.b = new Corner(b, this);
            this.c = new Corner(c, this);
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

    }

    private class Builder {

        private class OpenEdge {

            private final Vertex a, b;
            final Vec3 rollFrom;

            private OpenEdge(Vertex a, Vertex b, Vec3 rollFrom) {
                this.a = a;
                this.b = b;
                this.rollFrom = rollFrom;
            }

            public Line3 line() {
                return aToB(a, b);
            }

            public Edge edge() {
                return new Edge(a, b);
            }
        }

        private class RollingCollision {

            final Vec3 rolling;
            final Vertex vertex;

            private RollingCollision(Vec3 rolling, Vertex vertex) {
                this.rolling = rolling;
                this.vertex = vertex;
            }

        }

        private Function<Vertex, RollingCollision> vertexCollisionWith(final Vec3 rolling) {
            return new Function<Vertex, RollingCollision>() {
                public RollingCollision apply(Vertex vertex) {
                    return new RollingCollision(rolling, vertex);
                }
            };
        }

        final Balls<Vertex> balls;
        List<Triangle> triangles = newArrayList();
        List<Vertex> vertices = newArrayList();
        Map<Edge, OpenEdge> openEdges = newHashMap();
        Set<Edge> closedEdges = newHashSet();
        float rollingRadius;

        Builder(final Balls<Vertex> balls) {

            this.balls = balls;
            rollingRadius = balls.radius * rollingScale;

            final Vertex a = Ordering.natural().onResultOf(new Function<Vertex, Float>() {
                public Float apply(Vertex vertex) {
                    return vertex.loc.z();
                }
            }).max(balls.balls);

            vertices.add(a);

            final RollingCollision collision = Approximation
                .samplePointsAroundSphere(sphere(a, rollingRadius))
                .transformAndConcat(new Function<Vec3, Iterable<RollingCollision>>() {
                    public Iterable<RollingCollision> apply(final Vec3 rolling) {
                        return FluentIterable
                            .from(balls.balls)
                            .filter(new Predicate<Vertex>() {
                                public boolean apply(Vertex v) {
                                    return v != a && distance(v, rolling) < rollingRadius;
                                }
                            })
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
            tryEdge(new OpenEdge(a, b, rolling));
            openEdges.put(new Edge(b, a), new OpenEdge(b, a, rolling));

            while (openEdges.size() != 0) {

                Iterator<Entry<Edge,OpenEdge>> iterator = openEdges.entrySet().iterator();
                OpenEdge openEdge = iterator.next().getValue();
                iterator.remove();
                tryEdge(openEdge);
                closedEdges.add(openEdge.edge());

                if (Thread.interrupted()) {
                    return;
                }
            }
            //System.out.println(triangles.size());
        }

        void tryEdge(final OpenEdge openEdge) {

            final RollingCollision collision = Approximation
                .samplePointsAroundLine(openEdge.line(), openEdge.rollFrom)
                .transformAndConcat(new Function<Vec3, Iterable<RollingCollision>>() {
                    public Iterable<RollingCollision> apply(final Vec3 rolling) {
                        return FluentIterable
                            .from(balls.balls)
                            .filter(new Predicate<Vertex>() {
                                public boolean apply(Vertex v) {
                                    return v != openEdge.a && v != openEdge.b
                                        && !closedEdges.contains(new Edge(openEdge.a, v))
                                        && !closedEdges.contains(new Edge(openEdge.b, v))
                                        && distance(v, rolling) < rollingRadius;
                                }
                            })
                            .transform(vertexCollisionWith(rolling));
                    }
                })
                .first()
                .orNull();

            if (collision == null) {
                return;
            }

            vertices.add(collision.vertex);

            for (OpenEdge nextEdge : asList(
                new OpenEdge(openEdge.a, collision.vertex, collision.rolling),
                new OpenEdge(collision.vertex, openEdge.b, collision.rolling)
            )) {
                if (openEdges.remove(nextEdge.edge()) != null) {
                    closedEdges.add(nextEdge.edge());
                } else {
                    openEdges.put(nextEdge.edge(), nextEdge);
                }
            }

            triangles.add(new Triangle(openEdge.a, openEdge.b, collision.vertex));
        }

    }

}
