package tube;

import spray.Geometry.Vec3;

import static spray.Geometry.distance;
import static tube.Measures.d;
import static tube.Pt.P;
import static tube.Vec.A;
import static tube.Vec.V;

import static processing.core.PApplet.*;

public class Rotate {

    // rotated 90 degrees in XY plane
    public static Vec R(Vec V) {
        return V(-V.$.y(), V.$.x(), V.$.z());
    }

    // Rotated P by a around G in plane (I,J)
    public static Pt R(Pt P, float a, Vec I, Vec J, Pt G) {
        float x = d(V(G, P), I), y = d(V(G, P), J);
        float c = cos(a), s = sin(a);
        return P(P, x * c - x - y * s, I, x * s + y * c - y, J);
    }

    // Rotated V by a parallel to plane (I,J)
    public static Vec3 R(Vec3 V, float a, Vec3 I, Vec3 J) {
        float x = distance(V, I), y = distance(V, J);
        float c = cos(a), s = sin(a);
        return A(new Vec(V), V(x * c - x - y * s, new Vec(I), x * s + y * c - y, new Vec(J))).$;
    }

}
