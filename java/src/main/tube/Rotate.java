package tube;

import static tube.Pt.P;
import static tube.Vec.A;
import static tube.Vec.V;

import static processing.core.PApplet.*;
import static tube.Measures.*;

public class Rotate {

    // rotated 90 degrees in XY plane
    public static Vec R(Vec V) {
        return V(-V.y, V.x, V.z);
    }

    // Rotated P by a around G in plane (I,J)
    public static Pt R(Pt P, float a, Vec I, Vec J, Pt G) {
        float x = d(V(G, P), I), y = d(V(G, P), J);
        float c = cos(a), s = sin(a);
        return P(P, x * c - x - y * s, I, x * s + y * c - y, J);
    }

    // Rotated V by a parallel to plane (I,J)
    public static Vec R(Vec V, float a, Vec I, Vec J) {
        float x = d(V, I), y = d(V, J);
        float c = cos(a), s = sin(a);
        return A(V, V(x * c - x - y * s, I, x * s + y * c - y, J));
    }

}
