package spray;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import spray.Geometry.IsVec3;
import spray.Geometry.Line3;
import spray.Geometry.Vec3;

import static com.google.common.collect.Lists.newArrayList;
import static spray.Geometry.aToB;
import static spray.Geometry.distance;
import static spray.Geometry.pointAndStep;

public class Balls<V extends IsVec3> {

    public final Set<V> balls = new HashSet<V>();

    float radius = 20;

    public Balls() { }

    public Balls(Iterable<V> balls) {
        for (V ball : balls) {
            this.balls.add(ball);
        }
    }

    /**
     * Find the first ball the ray hits.
     */
    V raySearch(final Line3 ray) {
        try {
            return Ordering.natural()
                .onResultOf(
                    new Function<V, Float>() {
                        public Float apply(V ball) {
                            return distance(ball, ray.a());
                        }
                    }
                )
                .min(
                    FluentIterable.from(balls)
                        .filter(new Predicate<V>() {
                            public boolean apply(V ball) {
                                return distance(ray, ball) < radius;
                            }
                        })
                );
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Shoot a ball along the ray and pack it into the set.
     */
    Vec3 rayPack(final Line3 ray) {

        final V c1 = raySearch(ray);
        if (c1 == null || distance(c1, ray.a()) < 4 * radius) {
            return null;
        }

        final List<V> closeBalls = newArrayList(
            FluentIterable.from(balls)
                .filter(new Predicate<V>() {
                    public boolean apply(V ball) {
                        float d = distance(c1, ball);
                        return d > 0.1 && d < radius * 8;
                    }
                })
        );

        final Vec3 ball1 = c1.asVec3().add(ray.ab().mag(-2 * radius));

        final Line3 axis1 = pointAndStep(c1, ray.ab().orthog());

        Object[] ballAndC2 = Approximation.samplePointsAroundLine(axis1, ball1)
            .transformAndConcat(new Function<Vec3, Iterable<Object[]>>() {
                public Iterable<Object[]> apply(final Vec3 ball2) {
                    return FluentIterable
                        .from(closeBalls)
                        .filter(new Predicate<V>() {
                            public boolean apply(V closeBall) {
                                return distance(ball2, closeBall) < 2 * radius + 1;
                            }
                        })
                        .transform(new Function<V, Object[]>() {
                            public Object[] apply(V closeBall) {
                                return new Object[]{ ball2, closeBall };
                            }
                        });
                }
            })
            .first()
            .orNull();

        final Vec3 ball2;
        V c2;
        if (ballAndC2 != null) {
            ball2 = (Vec3) ballAndC2[0];
            c2 = (V) ballAndC2[1];
        } else {
            ball2 = ball1;
            c2 = null;
        }

        Vec3 ball3;
        if (c2 == null) {
            ball3 = ball2;
        } else {
            closeBalls.remove(c2);
            final Line3 axis2 = aToB(c1, c2);
            ball3 = Approximation.samplePointsAroundLine(axis2, ball2)
                .filter(new Predicate<Vec3>() {
                    public boolean apply(final Vec3 ball3) {
                        return FluentIterable
                            .from(closeBalls)
                            .anyMatch(new Predicate<V>() {
                                public boolean apply(V closeBall) {
                                    return distance(ball3, closeBall) < 2 * radius + 1;
                                }
                            });
                    }
                })
                .first()
                .or(ball2);
        }

        return ball3;
    }

}
