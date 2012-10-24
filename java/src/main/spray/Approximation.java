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
                        .closedOpen(0, 1000)
                        .asSet(DiscreteDomains.integers())
                )
                    .transform(new Function<Integer, Float>() {
                        public Float apply(Integer i) {
                            return (float) ((i / 1000.) * 2 * Math.PI);
                        }
                    }
                    )
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

    public static final FluentIterable<Vec3> SPIRAL =
        FluentIterable.from(
            FluentIterable
                .from(
                    Ranges
                        .closedOpen(0, 1000)
                        .asSet(DiscreteDomains.integers())
                )
                .transform(new Function<Integer, Vec3>() {
                    public Vec3 apply(Integer i) {
                        float elevation = -1 * (float) ((i / 1000.) * Math.PI - (Math.PI / 2));
                        float azimuth = (float) (2 * Math.PI * (i % 60) / 60.);
                        return Geometry.azimuthAndElevation(azimuth, elevation, 1);
                    }
                })
                .toImmutableList()
        );

    public static FluentIterable<Vec3> samplePointsAroundSphere(final Sphere sphere) {
        return SPIRAL
            .transform(new Function<Vec3, Vec3>() {
                public Vec3 apply(Vec3 p) {
                    return p.mag(sphere.radius()).add(sphere.center());
                }
            });
    }

}
