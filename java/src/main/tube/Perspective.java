package tube;

import static tube.Pt.P;

public class Perspective {

    static Pt Pers(Pt P, float d) {
        return P(d * P.x / (d + P.z), d * P.y / (d + P.z), d * P.z / (d + P.z));
    }

    static Pt InverserPers(Pt P, float d) {
        return P(d * P.x / (d - P.z), d * P.y / (d - P.z), d * P.z / (d - P.z));
    }

}
