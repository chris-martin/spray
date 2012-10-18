package tube;

import spray.Geometry.Vec3;

import static spray.Geometry.xyz;

public class Perspective {

    static Vec3 Pers(Vec3 P, float d) {
        return xyz(d * P.x() / (d + P.z()), d * P.y() / (d + P.z()), d * P.z() / (d + P.z()));
    }

    static Vec3 InverserPers(Vec3 P, float d) {
        return xyz(d * P.x() / (d - P.z()), d * P.y() / (d - P.z()), d * P.z() / (d - P.z()));
    }

}
