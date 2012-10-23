package spray;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Ranges;
import spray.Geometry.Line3;
import spray.Geometry.Vec3;

import static com.google.common.collect.Lists.newArrayList;
import static spray.Geometry.aToB;
import static spray.Geometry.distance;
import static spray.Geometry.pointAndStep;
import static spray.Geometry.rotatePointAroundLine;

public class Balls {

    private final Set<Vec3> balls = new HashSet<Vec3>();

    final int radius = 8;

    public void add(Vec3 ball) {
        balls.add(ball);
    }

    public void remove(Vec3 ball) {
        balls.remove(ball);
    }

    public FluentIterable<Vec3> iter() {
        return FluentIterable.from(balls);
    }

    /**
     * Find the first ball the ray hits.
     */
    Vec3 raySearch(final Line3 ray) {
        try {
            return Ordering.natural()
                .onResultOf(
                    new Function<Vec3, Float>() {
                        public Float apply(Vec3 ball) {
                            return distance(ball, ray.a());
                        }
                    }
                )
                .min(
                    iter()
                        .filter(new Predicate<Vec3>() {
                            public boolean apply(Vec3 ball) {
                                return distance(ray, ball) < radius + 1;
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

        final Vec3 c1 = raySearch(ray);
        if (c1 == null) {
            return null;
        }

        final List<Vec3> closeBalls = newArrayList(iter()
            .filter(new Predicate<Vec3>() {
                public boolean apply(Vec3 ball) {
                    float d = distance(c1, ball);
                    return d > 0.1 && d < radius * 4;
                }
            }));

        final Vec3 ball1 = c1.add(ray.ab().mag(-1 * radius));

        final Line3 axis1 = pointAndStep(c1, ray.ab().orthog());

        Vec3[] ballAndC2 = FluentIterable
            .from(Ranges.closedOpen(0, 1000).asSet(DiscreteDomains.integers()))
            .transform(new Function<Integer, Vec3>() {
                public Vec3 apply(Integer i) {
                    float angle = (float) ((i / 1000.) * 2 * Math.PI);
                    return rotatePointAroundLine(axis1, ball1, angle);
                }
            })
            .transformAndConcat(new Function<Vec3, Iterable<Vec3[]>>() {
                public Iterable<Vec3[]> apply(final Vec3 ball2) {
                    return FluentIterable
                        .from(closeBalls)
                        .filter(new Predicate<Vec3>() {
                            public boolean apply(Vec3 closeBall) {
                                return distance(ball2, closeBall) < radius + 1;
                            }
                        })
                        .transform(new Function<Vec3, Vec3[]>() {
                            public Vec3[] apply(Vec3 closeBall) {
                                return new Vec3[]{ball2, closeBall};
                            }
                        });
                }
            })
            .first()
            .orNull();

        final Vec3 ball2;
        Vec3 c2;
        if (ballAndC2 != null) {
            ball2 = ballAndC2[0];
            c2 = ballAndC2[1];
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
            return FluentIterable
                .from(Ranges.closedOpen(0, 1000).asSet(DiscreteDomains.integers()))
                .transform(new Function<Integer, Vec3>() {
                    public Vec3 apply(Integer i) {
                        float angle = (float) ((i / 1000.) * 2 * Math.PI);
                        return rotatePointAroundLine(axis2, ball2, angle);
                    }
                })
                .filter(new Predicate<Vec3>() {
                    public boolean apply(final Vec3 ball3) {
                        return FluentIterable
                            .from(closeBalls)
                            .anyMatch(new Predicate<Vec3>() {
                                public boolean apply(Vec3 closeBall) {
                                    return distance(ball3, closeBall) < radius + 2;
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
