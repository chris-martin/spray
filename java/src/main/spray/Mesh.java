package spray;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
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

public final class Mesh {

    int previousVertexId, previousTriangleId;

    List<Triangle> triangles = newArrayList();

    public List<Triangle> triangles() {
        return unmodifiableList(triangles);
    }

    public static float rollingScale = 3.5f;

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

        while (vBalls.balls.size() != 0) {
            Builder builder = new Builder(vBalls);
            triangles.addAll(builder.triangles);
            for (Vertex v : builder.vertices) {
                vBalls.balls.remove(v);
            }
        }

    }

    public class Vertex implements IsVec3 {

        private final int id = ++previousVertexId;

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

        public boolean equals(Object o) {
            return this == o || (o instanceof Edge && a == ((Edge) o).a && b == ((Edge) o).b);
        }

        public int hashCode() {
            return 31 * a.hashCode() + b.hashCode();
        }

    }

    public class Triangle {
        private final int id = ++previousTriangleId;

        public int hashCode() {
            return id;
        }

        private final Vertex a, b, c;

        public Triangle(Vertex a, Vertex b, Vertex c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public FluentIterable<Vertex> vertices() {
            return FluentIterable.from(asList(a, b, c));
        }

        public List<Edge> edges() {
            Vertex a = this.a, b = this.b, c = this.c;
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

            public String toString() {
                return String.format("%d %s to %d %s rolling from %s", a.id, a.loc, b.id, b.loc, rollFrom);
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
        }

        void tryEdge(final OpenEdge openEdge) {

            final RollingCollision collision = Approximation
                .samplePointsAroundLine(openEdge.line(), openEdge.rollFrom)
                .skip(3)
                .transformAndConcat(new Function<Vec3, Iterable<RollingCollision>>() {
                    public Iterable<RollingCollision> apply(final Vec3 rolling) {
                        /*triangles.add(new Triangle(
                            new Vertex(rolling),
                            new Vertex(rolling.addX(5)),
                            new Vertex(rolling.addY(5))
                        ));*/
                        return FluentIterable
                            .from(balls.balls)
                            .filter(new Predicate<Vertex>() {
                                public boolean apply(Vertex v) {
                                    return v != openEdge.a && v != openEdge.b
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
                if (!closedEdges.contains(nextEdge.edge())) {
                    if (openEdges.remove(nextEdge.edge()) != null) {
                        closedEdges.add(nextEdge.edge());
                    } else {
                        openEdges.put(nextEdge.edge(), nextEdge);
                    }
                }
            }

            triangles.add(new Triangle(openEdge.a, openEdge.b, collision.vertex));
        }

    }

}
