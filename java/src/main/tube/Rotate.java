package tube;

import spray.Geometry.Vec3;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static spray.Geometry.aToB;
import static spray.Geometry.distance;

public class Rotate {

    // Rotated P by a around G in plane (I,J)
    public static Vec3 R(Vec3 P, float a, Vec3 I, Vec3 J, Vec3 G) {
        float x = aToB(G, P).ab().dot(I),
              y = aToB(G, P).ab().dot(J);
        float c = cos(a), s = sin(a);
        return P
            .add(I.mult(x * c - x - y * s))
            .add(J.mult(x * s + y * c - y));
    }

    // Rotated V by a parallel to plane (I,J)
    public static Vec3 R(Vec3 V, float a, Vec3 I, Vec3 J) {
        float x = distance(V, I), y = distance(V, J);
        float c = cos(a), s = sin(a);
        return V
            .add(I.mult(x * c - x - y * s))
            .add(J.mult(x * s + y * c - y));
    }

}
