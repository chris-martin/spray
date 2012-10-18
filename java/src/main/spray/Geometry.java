package spray;

import static java.lang.Math.*;
import static spray.Geometry.Side.LEFT;
import static spray.Geometry.Side.RIGHT;

public final class Geometry {
    private Geometry() {
    }

    private static final float EPSILON = 0.000001f;

    private static final float PI = (float) Math.PI;
    private static final float PI2 = PI * 2;
    private static final float HALFPI = PI / 2;

    private static float angle(float a) {
        return a < 0 ? ((a % PI2) + PI2) : (a % PI2);
    }

    private static float angle(float a, boolean flip) {
        return angle(flip ? a + PI : a);
    }

    private static int sign(float x) {
        return x == 0 ? 0 : (x < 0 ? -1 : 1);
    }

    /**
     * A point in a Euclidean plane.
     */
    public static interface Vec2 extends Comparable<Vec2> {

        public float x();

        public float y();

        public float ang();

        public float mag();

        public Vec2 mag(float newMag);

        /**
         * Equivalent to mag(1).
         */
        public Vec2 unit();

        public Vec2 add(Vec2 o);

        public Vec2 sub(Vec2 o);

        public Vec2 mult(float factor);

        public Vec2 mult(Number factor);

        public Vec2 div(float divisor);

        public Vec2 div(Number divisor);

        public Vec2 rot(float ang);

        public Vec2 rot(Number ang);

        public Vec2 addX(float o);

        public Vec2 addY(float o);

        public Vec2 subX(float o);

        public Vec2 subY(float o);

        /**
         * This is exactly (0, 0).
         */
        public boolean isOrigin();

        public Vec2 rot90();

        public Vec2 rot180();

        /**
         * Scalar (dot) product.
         */
        public float dot(Vec2 o);

        /**
         * U x V = U dot rot90(V).
         */
        public float cross(Vec2 o);

    }

    private static abstract class BaseVec2 implements Vec2 {
        public int compareTo(Vec2 o) {
            return Float.compare(mag(), o.mag());
        }

        public Vec2 add(Vec2 o) {
            return new XY(this.x() + o.x(), this.y() + o.y());
        }

        public Vec2 sub(Vec2 o) {
            return new XY(this.x() - o.x(), this.y() - o.y());
        }

        public Vec2 mag(float newMag) {
            return new Ang2(ang(), newMag);
        }

        public Vec2 unit() {
            return new Ang2(ang(), 1);
        }

        public Vec2 mult(Number factor) {
            return mult(factor.floatValue());
        }

        public Vec2 div(Number divisor) {
            return div(divisor.floatValue());
        }

        public Vec2 addX(float $) {
            return xy(x() + $, y());
        }

        public Vec2 addY(float $) {
            return xy(x(), y() + $);
        }

        public Vec2 subX(float $) {
            return xy(x() - $, y());
        }

        public Vec2 subY(float $) {
            return xy(x(), y() - $);
        }

        public float dot(Vec2 o) {
            return x() * o.x() + y() * o.y();
        }

        public float cross(Vec2 o) {
            return dot(o.rot90());
        }

        public Vec2 rot(float ang) {
            return new Ang2(ang() + ang, mag());
        }

        public Vec2 rot(Number ang) {
            return rot(ang.floatValue());
        }

        public boolean isOrigin() {
            return false;
        }

        public String toString() {
            return String.format("(%f, %f)", x(), y());
        }
    }

    private static class XY extends BaseVec2 {
        final float x, y;
        float ang, mag;
        boolean hasAng, hasMag;

        XY(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public float ang() {
            if (!hasAng) {
                ang = (float) atan2(y, x);
                hasMag = true;
            }
            return ang;
        }

        public float mag() {
            if (!hasMag) {
                mag = (float) sqrt(pow(x, 2) + pow(y, 2));
                hasMag = true;
            }
            return mag;
        }

        public XY rot180() {
            return new XY(-1 * x, -1 * y);
        }

        public XY rot90() {
            return new XY(-1 * y, x);
        }

        public Vec2 mult(float f) {
            if (abs(f) < EPSILON) return origin2();
            return new XY(f * x, f * y);
        }

        public XY div(float d) {
            return new XY(x / d, y / d);
        }
    }

    public static Vec2 xy(float x, float y) {
        return abs(x) < EPSILON && abs(y) < EPSILON ? ORIGIN_2 : new XY(x, y);
    }

    public static Vec2 xy(Number x, Number y) {
        return xy(x.floatValue(), y.floatValue());
    }

    public static Vec2 xy(java.awt.Point p) {
        return new XY(p.x, p.y);
    }

    public static Vec2 xy(java.awt.event.MouseEvent e) {
        return new XY(e.getX(), e.getY());
    }

    private static class Ang2 extends BaseVec2 {
        final float ang, mag;
        float x, y;
        boolean hasXy;

        Ang2(float ang, float mag) {
            this.ang = angle(ang, mag < 0);
            this.mag = abs(mag);
        }

        Ang2(float ang) {
            this.ang = angle(ang);
            mag = 1;
        }

        private void ensureXy() {
            if (hasXy) return;
            x = mag * (float) cos(ang);
            y = mag * (float) sin(ang);
            hasXy = true;
        }

        public float x() {
            ensureXy();
            return x;
        }

        public float y() {
            ensureXy();
            return y;
        }

        public float ang() {
            return ang;
        }

        public float mag() {
            return mag;
        }

        public Ang2 rot180() {
            return new Ang2(ang + PI, mag);
        }

        public Ang2 rot90() {
            return new Ang2(ang + HALFPI, mag);
        }

        public Vec2 mult(float f) {
            if (abs(f) < EPSILON) return origin2();
            return new Ang2(ang, f * mag);
        }

        public Ang2 div(float d) {
            return new Ang2(ang, mag / d);
        }
    }

    public static Vec2 angleVec2(float ang, float mag) {
        return abs(mag) < EPSILON ? ORIGIN_2 : new Ang2(ang, mag);
    }

    public static Vec2 angleVec2(Number ang, Number mag) {
        return angleVec2(ang.floatValue(), mag.floatValue());
    }

    public static Vec2 angleVec2(float ang, Number mag) {
        return angleVec2(ang, mag.floatValue());
    }

    public static Vec2 angleVec2(Number ang, float mag) {
        return angleVec2(ang.floatValue(), mag);
    }

    public static Vec2 angleVec2(float ang) {
        return new Ang2(ang);
    }

    public static Vec2 angleVec2(Number ang) {
        return new Ang2(ang.floatValue());
    }

    private static class Origin2 implements Vec2 {
        public float x() {
            return 0;
        }

        public float y() {
            return 0;
        }

        public float ang() {
            return 0;
        }

        public float mag() {
            return 0;
        }

        public Vec2 mult(float factor) {
            return this;
        }

        public Vec2 div(float divisor) {
            return this;
        }

        public Vec2 rot90() {
            return this;
        }

        public Vec2 rot180() {
            return this;
        }

        public Vec2 add(Vec2 o) {
            return o;
        }

        public Vec2 sub(Vec2 o) {
            return o.mult(-1);
        }

        public Vec2 addX(float $) {
            return xy($, 0);
        }

        public Vec2 addY(float $) {
            return xy(0, $);
        }

        public Vec2 subX(float $) {
            return xy(-$, 0);
        }

        public Vec2 subY(float $) {
            return xy(0, -$);
        }

        public Vec2 mag(float newMag) {
            return this;
        }

        public Vec2 unit() {
            return this;
        }

        public Vec2 mult(Number factor) {
            return this;
        }

        public Vec2 div(Number divisor) {
            return this;
        }

        public float dot(Vec2 o) {
            return 0;
        }

        public float cross(Vec2 o) {
            return 0;
        }

        public Vec2 rot(float ang) {
            return this;
        }

        public Vec2 rot(Number ang) {
            return this;
        }

        public int compareTo(Vec2 o) {
            return Float.compare(0, o.mag());
        }

        public boolean isOrigin() {
            return true;
        }
    }

    private static final Origin2 ORIGIN_2 = new Origin2();

    public static Vec2 origin2() {
        return ORIGIN_2;
    }

    /**
     * A directed line segment in a Euclidean plane.
     */
    public static interface Line2 {

        Vec2 a();

        Vec2 b();

        Vec2 ab();

        float mag();

        float ang();

        Side side(Vec2 p);

        Line2 add(Vec2 offset);

        Line2 sub(Vec2 offset);

        Vec2 midpoint();

        Line2 bisect();

        /**
         * Not equal, but correlated, to "bulge" as defined by Jarek Rossignac.
         */
        float bulge(Vec2 p);

    }

    public static enum Side {
        LEFT(-1), RIGHT(1);
        final int i;

        Side(int i) {
            this.i = i;
        }

        Side opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    private static abstract class BaseLine2 implements Line2 {
        public Side side(Vec2 p) {
            return p.sub(a()).cross(b().sub(a())) > 0 ? LEFT : RIGHT;
        }

        public Line2 bisect() {
            return pointAndStep(midpoint(), Geometry.angleVec2(ang()).rot90());
        }

        public float bulge(Vec2 p) {
            Circle2 c = circle(a(), b(), p);
            return c.radius() * side(p).i * side(c.center()).i;
        }
    }

    private static class OriginLine2 extends BaseLine2 {
        final Vec2 b;

        OriginLine2(Vec2 b) {
            this.b = b;
        }

        public Vec2 a() {
            return ORIGIN_2;
        }

        public Vec2 b() {
            return b;
        }

        public Vec2 ab() {
            return b;
        }

        public float ang() {
            return b.ang();
        }

        public float mag() {
            return b.mag();
        }

        public Line2 add(Vec2 offset) {
            return aToB(offset, b.add(offset));
        }

        public Line2 sub(Vec2 offset) {
            return aToB(offset.mult(-1), b.sub(offset));
        }

        public Vec2 midpoint() {
            return b.div(2);
        }
    }

    public static Line2 oTo2(float ang) {
        return new OriginLine2(Geometry.angleVec2(ang));
    }

    public static Line2 oTo2(Number ang) {
        return new OriginLine2(angleVec2(ang));
    }

    public static Line2 oTo2(Vec2 p) {
        return new OriginLine2(p);
    }

    private static class AtoB2 extends BaseLine2 {
        final Vec2 a, b;
        Vec2 ab;

        AtoB2(Vec2 a, Vec2 b) {
            this.a = a;
            this.b = b;
        }

        AtoB2(Vec2 a, Vec2 b, Vec2 ab) {
            this.a = a;
            this.b = b;
            this.ab = ab;
        }

        public Vec2 a() {
            return a;
        }

        public Vec2 b() {
            return b;
        }

        public Vec2 ab() {
            if (ab == null) ab = b.sub(a);
            return ab;
        }

        public float ang() {
            return ab().ang();
        }

        public float mag() {
            return ab().mag();
        }

        public Line2 add(Vec2 offset) {
            return new AtoB2(offset.add(a), b.add(offset), ab);
        }

        public Line2 sub(Vec2 offset) {
            return new AtoB2(offset.sub(a), b.sub(offset), ab);
        }

        public Vec2 midpoint() {
            return a.add(b).div(2);
        }

        public String toString() {
            return String.format("Line %s to %s", a, b);
        }
    }

    public static Line2 aToB(Vec2 a, Vec2 b) {
        return new AtoB2(a, b);
    }

    private static class PointAndDirection extends BaseLine2 {
        final Vec2 a, ab;
        Vec2 b;

        PointAndDirection(Vec2 a, Vec2 ab) {
            this.a = a;
            this.ab = ab;
        }

        public Vec2 a() {
            return a;
        }

        public Vec2 b() {
            if (b == null) b = a.add(ab);
            return b;
        }

        public Vec2 ab() {
            return ab;
        }

        public float ang() {
            return ab.ang();
        }

        public float mag() {
            return ab.mag();
        }

        public Line2 add(Vec2 offset) {
            return pointAndStep(a.add(offset), ab);
        }

        public Line2 sub(Vec2 offset) {
            return pointAndStep(a.sub(offset), ab);
        }

        public Vec2 midpoint() {
            return ab.div(2).add(a);
        }
    }

    public static Line2 pointAndStep(Vec2 a, Vec2 ab) {
        return new PointAndDirection(a, ab);
    }

    public static Line2 pointAndStep(Vec2 a, float ang) {
        return new PointAndDirection(a, Geometry.angleVec2(ang));
    }

    public static Line2 pointAndStep(Vec2 a, Number ang) {
        return new PointAndDirection(a, angleVec2(ang));
    }

    public static Vec2 intersect(Line2 ab, Line2 cd) {
        Vec2 v1 = ab.a(), v2 = ab.b(), v3 = cd.a(), v4 = cd.b();
        // http://en.wikipedia.org/wiki/Line-line_intersection
        float x1 = v1.x(), y1 = v1.y(), x2 = v2.x(), y2 = v2.y(), x3 = v3.x(), y3 = v3.y(), x4 = v4.x(), y4 = v4.y();
        float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        float x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        float y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
        return xy(x, y);
    }

    public static boolean overlap(Line2 ab, Line2 cd) {
        Vec2 a = ab.a(), b = ab.b(), c = cd.a(), d = cd.b();
        return ab.side(c) != ab.side(d) && cd.side(a) != cd.side(b);
    }

    public static interface Circle2 {
        Vec2 center();

        float radius();
    }

    private static class SimpleCircle2 implements Circle2 {
        final Vec2 center;
        final float radius;

        SimpleCircle2(Vec2 center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        public Vec2 center() {
            return center;
        }

        public float radius() {
            return radius;
        }
    }

    public static Circle2 circle(Vec2 center, float radius) {
        return new SimpleCircle2(center, radius);
    }

    public static Circle2 circle(Vec2 center, Number radius) {
        return new SimpleCircle2(center, radius.floatValue());
    }

    private static class TriangleCircle2 implements Circle2 {
        final Vec2[] vs;
        Vec2 center;
        float radius;
        boolean hasRadius;

        TriangleCircle2(Vec2[] vs) {
            this.vs = vs;
        }

        public Vec2 center() {
            if (center == null) _center();
            return center;
        }

        void _center() {
            center = intersect(aToB(vs[0], vs[1]).bisect(), aToB(vs[1], vs[2]).bisect());
        }

        public float radius() {
            if (!hasRadius) _radius();
            return radius;
        }

        void _radius() {
            radius = center().sub(vs[0]).mag();
            hasRadius = true;
        }
    }

    public static Circle2 circle(Vec2 a, Vec2 b, Vec2 c) {
        return new TriangleCircle2(new Vec2[]{a, b, c});
    }

    /**
     * 0, 1, or 2 intersections.
     */
    public static Vec2[] intersect(Line2 line, Circle2 circle) {
        // http://mathworld.wolfram.com/Circle-LineIntersection.html
        float r = circle.radius();
        Vec2 cc = circle.center();
        line = line.sub(cc);
        Vec2 a = line.a(), b = line.b(), ab = line.ab();
        float dx = ab.x(), dy = ab.y();
        float dr = (float) sqrt(pow(dx, 2) + pow(dy, 2));
        float D = a.x() * b.y() - b.x() * a.y();
        float q = (float) sqrt(pow(r, 2) * pow(dr, 2) - pow(D, 2));
        if (q < 0) return new Vec2[0];
        float qx = sign(dy) * dx * q, qy = abs(dy) * q;
        float Ddy = D * dy, nDdx = 0 - D * dx;
        if (qx == 0 && qy == 0) return new Vec2[]{xy(Ddy, nDdx)};
        Vec2[] is = new Vec2[]{xy(Ddy + qx, nDdx + qy), xy(Ddy - qx, nDdx - qy)};
        for (int i = 0; i < 2; i++) is[i] = is[i].div(pow(dr, 2)).add(cc);
        return is;
    }


    /**
     * A point in three dimensions.
     */
    public static interface Vec3 extends Comparable<Vec3> {

        public float x();

        public float y();

        public float z();

        public float mag();

        public Vec3 mag(float newMag);

        /**
         * Equivalent to mag(1).
         */
        public Vec3 unit();

        public Vec3 add(Vec3 o);

        public Vec3 sub(Vec3 o);

        public Vec3 mult(float factor);

        public Vec3 mult(Number factor);

        public Vec3 div(float divisor);

        public Vec3 div(Number divisor);

        public Vec3 addX(float o);

        public Vec3 addY(float o);

        public Vec3 addZ(float o);

        public Vec3 subX(float o);

        public Vec3 subY(float o);

        public Vec3 subZ(float o);

        /**
         * This is exactly (0, 0, 0).
         */
        public boolean isOrigin();

        /**
         * Scalar (dot) product.
         */
        public float dot(Vec3 o);

        /**
         * Cross product U X V, normal to both U and V.
         */
        public Vec3 cross(Vec3 o);

    }

    private static abstract class BaseVec3 implements Vec3 {
        public int compareTo(Vec3 o) {
            return Float.compare(mag(), o.mag());
        }

        public Vec3 add(Vec3 o) {
            return new XYZ(this.x() + o.x(), this.y() + o.y(), this.z() + o.z());
        }

        public Vec3 sub(Vec3 o) {
            return new XYZ(this.x() - o.x(), this.y() - o.y(), this.z() - o.z());
        }

        public Vec3 mag(float newMag) {
            return unit().mult(newMag);
        }

        public Vec3 unit() {
            return div(mag());
        }

        public Vec3 mult(Number factor) {
            return mult(factor.floatValue());
        }

        public Vec3 div(Number divisor) {
            return div(divisor.floatValue());
        }

        public Vec3 addX(float $) {
            return xyz(x() + $, y(), z());
        }

        public Vec3 addY(float $) {
            return xyz(x(), y() + $, z());
        }

        public Vec3 addZ(float $) {
            return xyz(x(), y(), z() + $);
        }

        public Vec3 subX(float $) {
            return xyz(x() - $, y(), z());
        }

        public Vec3 subY(float $) {
            return xyz(x(), y() - $, z());
        }

        public Vec3 subZ(float $) {
            return xyz(x(), y(), z() - $);
        }

        public float dot(Vec3 o) {
            return x() * o.x() + y() * o.y() + z() * o.z();
        }

        public Vec3 cross(Vec3 o) {
            return xyz(
                y() * o.z() - z() * o.y(),
                z() * o.x() - x() * o.z(),
                x() * o.y() - y() * o.x()
            );
        }

        public boolean isOrigin() {
            return false;
        }

        public String toString() {
            return String.format("(%f, %f, %f)", x(), y(), z());
        }
    }

    private static class XYZ extends BaseVec3 {
        final float x, y, z;
        float mag;
        boolean hasMag;

        XYZ(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public float z() {
            return z;
        }

        public float mag() {
            if (!hasMag) {
                mag = (float) sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2));
                hasMag = true;
            }
            return mag;
        }

        public Vec3 mult(float f) {
            if (abs(f) < EPSILON) return origin3();
            return new XYZ(f * x, f * y, f * z);
        }

        public XYZ div(float d) {
            return new XYZ(x / d, y / d, z / d);
        }
    }

    public static Vec3 xyz(float x, float y, float z) {
        return abs(x) < EPSILON && abs(y) < EPSILON && abs(z) < EPSILON ? ORIGIN_3 : new XYZ(x, y, z);
    }

    public static Vec3 xyz(Number x, Number y, Number z) {
        return xyz(x.floatValue(), y.floatValue(), z.floatValue());
    }

    private static class Origin3 implements Vec3 {
        public float x() {
            return 0;
        }

        public float y() {
            return 0;
        }

        public float z() {
            return 0;
        }

        public float mag() {
            return 0;
        }

        public Vec3 mult(float factor) {
            return this;
        }

        public Vec3 div(float divisor) {
            return this;
        }

        public Vec3 add(Vec3 o) {
            return o;
        }

        public Vec3 sub(Vec3 o) {
            return o.mult(-1);
        }

        public Vec3 addX(float $) {
            return xyz($, 0, 0);
        }

        public Vec3 addY(float $) {
            return xyz(0, $, 0);
        }

        public Vec3 addZ(float $) {
            return xyz(0, 0, $);
        }

        public Vec3 subX(float $) {
            return xyz(-$, 0, 0);
        }

        public Vec3 subY(float $) {
            return xyz(0, -$, 0);
        }

        public Vec3 subZ(float $) {
            return xyz(0, 0, -$);
        }

        public Vec3 mag(float newMag) {
            return this;
        }

        public Vec3 unit() {
            return this;
        }

        public Vec3 mult(Number factor) {
            return this;
        }

        public Vec3 div(Number divisor) {
            return this;
        }

        public float dot(Vec3 o) {
            return 0;
        }

        public Vec3 cross(Vec3 o) {
            return this;
        }

        public int compareTo(Vec3 o) {
            return Float.compare(0, o.mag());
        }

        public boolean isOrigin() {
            return true;
        }
    }

    private static final Origin3 ORIGIN_3 = new Origin3();

    public static Vec3 origin3() {
        return ORIGIN_3;
    }

    public static float distance(Vec2 a, Vec2 b) {
        return a.sub(b).mag();
    }

    public static float distance(Vec3 a, Vec3 b) {
        return a.sub(b).mag();
    }

}
