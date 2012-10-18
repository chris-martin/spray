package spray;

import static java.lang.Math.*;
import static spray.Geometry.Side.LEFT;
import static spray.Geometry.Side.RIGHT;

public final class Geometry {
  private Geometry() {}

  private static final double PI2 = PI*2;
  private static final double HALFPI = PI/2;
  private static double angle(double a) { return a < 0 ? ((a % PI2) + PI2) : (a % PI2); }
  private static double angle(double a, boolean flip) { return angle(flip ? a + PI : a); }
  private static int sign(double x) { return x == 0 ? 0 : (x < 0 ? -1 : 1); }

  /** A point in a Euclidean plane. */
  public static interface Vec2 extends Comparable<Vec2> {

    public double x();
    public double y();

    public double ang();

    public double mag();
    public Vec2 mag(double newMag);

    /** Equivalent to mag(1). */
    public Vec2 unit();

    public Vec2 add(Vec2 o);
    public Vec2 sub(Vec2 o);
    public Vec2 mult(double factor);
    public Vec2 mult(Number factor);
    public Vec2 div(double divisor);
    public Vec2 div(Number divisor);
    public Vec2 rot(double ang);
    public Vec2 rot(Number ang);

    public Vec2 addX(double o);
    public Vec2 addY(double o);
    public Vec2 subX(double o);
    public Vec2 subY(double o);

    /** This is exactly (0, 0). */
    public boolean isOrigin();

    public Vec2 rot90();
    public Vec2 rot180();

    /** Scalar (dot) product. */
    public double dot(Vec2 o);

    /** U x V = U dot rot90(V). */
    public double cross(Vec2 o);

  }

  private static abstract class BaseVec2 implements Vec2 {
    public int compareTo(Vec2 o) { return Double.compare(mag(), o.mag()); }
    public Vec2 add(Vec2 o) { return new XY(this.x()+o.x(), this.y()+o.y()); }
    public Vec2 sub(Vec2 o) { return new XY(this.x()-o.x(), this.y()-o.y()); }
    public Vec2 mag(double newMag) { return new Ang2(ang(), newMag); }
    public Vec2 unit() { return new Ang2(ang(), 1); }
    public Vec2 mult(Number factor) { return mult(factor.doubleValue()); }
    public Vec2 div(Number divisor) { return div(divisor.doubleValue()); }
    public Vec2 addX(double $) { return xy(x()+$, y()); }
    public Vec2 addY(double $) { return xy(x(), y()+$); }
    public Vec2 subX(double $) { return xy(x() - $, y()); }
    public Vec2 subY(double $) { return xy(x(), y() - $); }
    public double dot(Vec2 o) { return x()*o.x() + y()*o.y(); }
    public double cross(Vec2 o) { return dot(o.rot90()); }
    public Vec2 rot(double ang) { return new Ang2(ang() + ang, mag()); }
    public Vec2 rot(Number ang) { return rot(ang.doubleValue()); }
    public boolean isOrigin() { return false; }
    public String toString() { return String.format("(%f, %f)", x(), y()); }
  }

  private static class XY extends BaseVec2 {
    final double x, y; double ang, mag; boolean hasAng, hasMag;
    XY(double x, double y) { this.x = x; this.y = y; }
    public double x() { return x; }
    public double y() { return y; }
    public double ang() { if (!hasAng) { ang = atan2(y, x); hasMag = true; } return ang; }
    public double mag() { if (!hasMag) { mag = sqrt(pow(x,2)+pow(y,2)); hasMag = true; } return mag; }
    public XY rot180() { return new XY(-1*x, -1*y); }
    public XY rot90() { return new XY(-1 * y, x); }
    public XY mult(double f) { return new XY(f*x, f*y); }
    public XY div(double d) { return new XY(x/d, y/d); }
  }
  public static Vec2 xy(double x, double y) { return x == 0 && y == 0 ? ORIGIN : new XY(x, y); }
  public static Vec2 xy(Number x, Number y) { return xy(x.doubleValue(), y.doubleValue()); }
  public static Vec2 xy(java.awt.Point p) { return new XY(p.x, p.y); }
  public static Vec2 xy(java.awt.event.MouseEvent e) { return new XY(e.getX(), e.getY()); }

  private static class Ang2 extends BaseVec2 {
    final double ang, mag; double x, y; boolean hasXy;
    Ang2(double ang, double mag) { this.ang = angle(ang, mag<0); this.mag = abs(mag); }
    Ang2(double ang) { this.ang = angle(ang); mag = 1; }
    private void ensureXy() { if (hasXy) return; x = mag * cos(ang); y = mag * sin(ang); hasXy = true; }
    public double x() { ensureXy(); return x; }
    public double y() { ensureXy(); return y; }
    public double ang() { return ang; }
    public double mag() { return mag; }
    public Ang2 rot180() { return new Ang2(ang+PI, mag); }
    public Ang2 rot90() { return new Ang2(ang+HALFPI, mag); }
    public Ang2 mult(double f) { return new Ang2(ang, f*mag); }
    public Ang2 div(double d) { return new Ang2(ang, mag/d); }
  }
  public static Vec2 angleVec2(double ang, double mag) { return mag == 0 ? ORIGIN : new Ang2(ang, mag); }
  public static Vec2 angleVec2(Number ang, Number mag) { return angleVec2(ang.doubleValue(), mag.doubleValue()); }
  public static Vec2 angleVec2(double ang, Number mag) { return angleVec2(ang, mag.doubleValue()); }
  public static Vec2 angleVec2(Number ang, double mag) { return angleVec2(ang.doubleValue(), mag); }
  public static Vec2 angleVec2(double ang) { return new Ang2(ang); }
  public static Vec2 angleVec2(Number ang) { return new Ang2(ang.doubleValue()); }

  private static class Origin implements Vec2 {
    public double x() { return 0; }
    public double y() { return 0; }
    public double ang() { throw new UnsupportedOperationException(); }
    public double mag() { return 0; }
    public Vec2 mult(double factor) { return this; }
    public Vec2 div(double divisor) { return this; }
    public Vec2 rot90() { return this; }
    public Vec2 rot180() { return this; }
    public Vec2 add(Vec2 o) { return o; }
    public Vec2 sub(Vec2 o) { return o.mult(-1); }
    public Vec2 addX(double $) { return xy($, 0); }
    public Vec2 addY(double $) { return xy(0, $); }
    public Vec2 subX(double $) { return xy(-$, 0); }
    public Vec2 subY(double $) { return xy(0, -$); }
    public Vec2 mag(double newMag) { throw new UnsupportedOperationException(); }
    public Vec2 unit() { throw new UnsupportedOperationException(); }
    public Vec2 mult(Number factor) { return this; }
    public Vec2 div(Number divisor) { return this; }
    public double dot(Vec2 o) { return 0; }
    public double cross(Vec2 o) { return 0; }
    public Vec2 rot(double ang) { return this; }
    public Vec2 rot(Number ang) { return this; }
    public int compareTo(Vec2 o) { return Double.compare(0, o.mag()); }
    public boolean isOrigin() { return true; }
  }
  private static final Origin ORIGIN = new Origin();
  public static Vec2 origin2() { return ORIGIN; }

  /** A directed line segment in a Euclidean plane. */
  public static interface Line2 {

    Vec2 a();
    Vec2 b();
    Vec2 ab();

    double mag();
    double ang();
    Side side(Vec2 p);
    Line2 add(Vec2 offset);
    Line2 sub(Vec2 offset);
    Vec2 midpoint();
    Line2 bisect();

    /** Not equal, but correlated, to "bulge" as defined by Jarek Rossignac. */
    double bulge(Vec2 p);

  }

  public static enum Side { LEFT(-1), RIGHT(1);
    final int i; Side(int i) { this.i = i; }
    Side opposite() { return this == LEFT ? RIGHT : LEFT; }
  }

  private static abstract class BaseLine2 implements Line2 {
    public Side side(Vec2 p) { return p.sub(a()).cross(b().sub(a())) > 0 ? LEFT : RIGHT; }
    public Line2 bisect() { return pointAndStep(midpoint(), Geometry.angleVec2(ang()).rot90()); }
    public double bulge(Vec2 p) { Circle2 c = circle(a(), b(), p); return c.radius() * side(p).i * side(c.center()).i; }
  }

  private static class OriginLine2 extends BaseLine2 {
    final Vec2 b;
    OriginLine2(Vec2 b) { this.b = b; }
    public Vec2 a() { return ORIGIN; }
    public Vec2 b() { return b; }
    public Vec2 ab() { return b; }
    public double ang() { return b.ang(); }
    public double mag() { return b.mag(); }
    public Line2 add(Vec2 offset) { return aToB(offset, b.add(offset)); }
    public Line2 sub(Vec2 offset) { return aToB(offset.mult(-1), b.sub(offset)); }
    public Vec2 midpoint() { return b.div(2); }
  }
  public static Line2 oTo2(double ang) { return new OriginLine2(Geometry.angleVec2(ang)); }
  public static Line2 oTo2(Number ang) { return new OriginLine2(angleVec2(ang)); }
  public static Line2 oTo2(Vec2 p) { return new OriginLine2(p); }

  private static class AtoB2 extends BaseLine2 {
    final Vec2 a, b; Vec2 ab;
    AtoB2(Vec2 a, Vec2 b) { this.a = a; this.b = b; }
    AtoB2(Vec2 a, Vec2 b, Vec2 ab) { this.a = a; this.b = b; this.ab = ab; }
    public Vec2 a() { return a; }
    public Vec2 b() { return b; }
    public Vec2 ab() { if (ab == null) ab = b.sub(a); return ab; }
    public double ang() { return ab().ang(); }
    public double mag() { return ab().mag(); }
    public Line2 add(Vec2 offset) { return new AtoB2(offset.add(a), b.add(offset), ab); }
    public Line2 sub(Vec2 offset) { return new AtoB2(offset.sub(a), b.sub(offset), ab); }
    public Vec2 midpoint() { return a.add(b).div(2); }
    public String toString() { return String.format("Line %s to %s", a, b); }
  }
  public static Line2 aToB(Vec2 a, Vec2 b) { return new AtoB2(a, b); }

  private static class PointAndDirection extends BaseLine2 {
    final Vec2 a, ab; Vec2 b;
    PointAndDirection(Vec2 a, Vec2 ab) { this.a = a; this.ab = ab; }
    public Vec2 a() { return a; }
    public Vec2 b() { if (b == null) b = a.add(ab); return b; }
    public Vec2 ab() { return ab; }
    public double ang() { return ab.ang(); }
    public double mag() { return ab.mag(); }
    public Line2 add(Vec2 offset) { return pointAndStep(a.add(offset), ab); }
    public Line2 sub(Vec2 offset) { return pointAndStep(a.sub(offset), ab); }
    public Vec2 midpoint() { return ab.div(2).add(a); }
  }
  public static Line2 pointAndStep(Vec2 a, Vec2 ab) { return new PointAndDirection(a, ab); }
  public static Line2 pointAndStep(Vec2 a, double ang) { return new PointAndDirection(a, Geometry.angleVec2(ang)); }
  public static Line2 pointAndStep(Vec2 a, Number ang) { return new PointAndDirection(a, angleVec2(ang)); }

  public static Vec2 intersect(Line2 ab, Line2 cd) { Vec2 v1 = ab.a(), v2 = ab.b(), v3 = cd.a(), v4 = cd.b();
    // http://en.wikipedia.org/wiki/Line-line_intersection
    double x1 = v1.x(), y1 = v1.y(), x2 = v2.x(), y2 = v2.y(), x3 = v3.x(), y3 = v3.y(), x4 = v4.x(), y4 = v4.y();
    double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
    double x = ((x1*y2-y1*x2)*(x3-x4) - (x1-x2)*(x3*y4-y3*x4)) / d;
    double y = ((x1*y2-y1*x2)*(y3-y4) - (y1-y2)*(x3*y4-y3*x4)) / d;
    return xy(x, y); }

  public static boolean overlap(Line2 ab, Line2 cd) { Vec2 a = ab.a(), b = ab.b(), c = cd.a(), d = cd.b();
    return ab.side(c) != ab.side(d) && cd.side(a) != cd.side(b); }

  public static interface Circle2 {
    Vec2 center();
    double radius();
  }

  private static class SimpleCircle2 implements Circle2 {
    final Vec2 center; final double radius;
    SimpleCircle2(Vec2 center, double radius) { this.center = center; this.radius = radius; }
    public Vec2 center() { return center; }
    public double radius() { return radius; }
  }
  public static Circle2 circle(Vec2 center, double radius) { return new SimpleCircle2(center, radius); }
  public static Circle2 circle(Vec2 center, Number radius) { return new SimpleCircle2(center, radius.doubleValue()); }

  private static class TriangleCircle2 implements Circle2 {
    final Vec2[] vs; Vec2 center; double radius; boolean hasRadius;
    TriangleCircle2(Vec2[] vs) { this.vs = vs; }
    public Vec2 center() { if (center == null) _center(); return center; }
    void _center() { center = intersect(aToB(vs[0], vs[1]).bisect(), aToB(vs[1], vs[2]).bisect()); }
    public double radius() { if (!hasRadius) _radius(); return radius; }
    void _radius() { radius = center().sub(vs[0]).mag(); hasRadius = true; }
  }
  public static Circle2 circle(Vec2 a, Vec2 b, Vec2 c) { return new TriangleCircle2(new Vec2[]{a,b,c}); }

  /** 0, 1, or 2 intersections. */
  public static Vec2[] intersect(Line2 line, Circle2 circle) {
    // http://mathworld.wolfram.com/Circle-LineIntersection.html
    double r = circle.radius(); Vec2 cc = circle.center(); line = line.sub(cc);
    Vec2 a = line.a(), b = line.b(), ab = line.ab();
    double dx = ab.x(), dy = ab.y();
    double dr = sqrt(pow(dx,2) + pow(dy,2));
    double D = a.x()*b.y() - b.x()*a.y();
    double q = sqrt(pow(r,2) * pow(dr,2) - pow(D,2));
    if (q < 0) return new Vec2[0];
    double qx = sign(dy) * dx * q, qy = abs(dy) * q;
    double Ddy = D*dy, nDdx = 0-D*dx;
    if (qx == 0 && qy == 0) return new Vec2[]{ xy(Ddy, nDdx) };
    Vec2[] is = new Vec2[]{ xy(Ddy+qx, nDdx+qy), xy(Ddy-qx, nDdx-qy) };
    for (int i = 0; i < 2; i++) is[i] = is[i].div(pow(dr, 2)).add(cc);
    return is;
  }

}
