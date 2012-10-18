package tube;

import spray.Geometry.Vec3;

import static tube.Pt.P;

public class Perspective {

    static Pt Pers(Vec3 P, float d) {
        return P(d * P.x() / (d + P.z()), d * P.y() / (d + P.z()), d * P.z() / (d + P.z()));
    }

    static Pt InverserPers(Vec3 P, float d) {
        return P(d * P.x() / (d - P.z()), d * P.y() / (d - P.z()), d * P.z() / (d - P.z()));
    }

}
