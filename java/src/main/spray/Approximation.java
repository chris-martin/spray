package spray;

import com.google.common.base.Function;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ranges;
import spray.Geometry.Line3;
import spray.Geometry.Sphere;
import spray.Geometry.Vec3;

public final class Approximation {

    private Approximation() { }

    public static final FluentIterable<Float> ANGLES =
        FluentIterable.from(
            FluentIterable
                .from(
                    Ranges
                        .closedOpen(0, 90)
                        .asSet(DiscreteDomains.integers())
                )
                .transform(new Function<Integer, Float>() {
                    public Float apply(Integer i) {
                        return (float) ((i / 90.) * 2 * Math.PI);
                    }
                })
                .toImmutableList()
        );

    public static FluentIterable<Vec3> samplePointsAroundLine(final Line3 axis, final Vec3 point) {
        return ANGLES
            .transform(new Function<Float, Vec3>() {
                public Vec3 apply(Float angle) {
                    return Geometry.rotatePointAroundLine(axis, point, angle);
                }
            });
    }

    public static FluentIterable<Vec3> spiral(final int points, final float loops) {
        final float pointsPerLoop = points / loops;
        return FluentIterable
            .from(
                Ranges
                    .closedOpen(0, points)
                    .asSet(DiscreteDomains.integers())
            )
            .transform(new Function<Integer, Vec3>() {
                public Vec3 apply(Integer i) {
                    float elevation = -1 * (float) ((i / (float) points) * Math.PI - (Math.PI / 2));
                    float azimuth = (float) (2 * Math.PI * (i % pointsPerLoop) / pointsPerLoop);
                    return Geometry.azimuthAndElevation(azimuth, elevation, 1);
                }
            });
    }

    public static final FluentIterable<Vec3> SPIRAL =
        FluentIterable.from(spiral(1000, 16.7f).toImmutableList());

    public static FluentIterable<Vec3> samplePointsAroundSphere(final Sphere sphere) {
        return SPIRAL
            .transform(new Function<Vec3, Vec3>() {
                public Vec3 apply(Vec3 p) {
                    return p.mag(sphere.radius()).add(sphere.center());
                }
            });
    }

}
