package tube;

import static tube.Measures.cw;
import static tube.Measures.m;
import static tube.Pt.P;
import static tube.Vec.V;

public class Intersection {

    // if (P,Q) intersects (A,B,C), return true and set X to the intersection point
    public static boolean intersect(Pt P, Pt Q, Pt A, Pt B, Pt C, Pt X) {
        return intersect(P, V(P, Q), A, B, C, X);
    }

    // if ray from E along T intersects triangle (A,B,C), return true and set X to the intersection point
    public static boolean intersect(Pt E, Vec T, Pt A, Pt B, Pt C, Pt X) {
        Vec EA = V(E, A), EB = V(E, B), EC = V(E, C), AB = V(A, B), AC = V(A, C);
        boolean s = cw(EA, EB, EC), sA = cw(T, EB, EC), sB = cw(EA, T, EC), sC = cw(EA, EB, T);
        if ((s == sA) && (s == sB) && (s == sC)) {
            return false;
        }
        float t = m(EA, AC, AB) / m(T, AC, AB);
        X.set(P(E, t, T));
        return true;
    }

    // true if ray from E with direction T hits triangle (A,B,C)
    public static boolean rayIntersectsTriangle(Pt E, Vec T, Pt A, Pt B, Pt C) {
        Vec EA = V(E, A), EB = V(E, B), EC = V(E, C);
        boolean s = cw(EA, EB, EC), sA = cw(T, EB, EC), sB = cw(EA, T, EC), sC = cw(EA, EB, T);
        return (s == sA) && (s == sB) && (s == sC);
    }

    public static boolean edgeIntersectsTriangle(Pt P, Pt Q, Pt A, Pt B, Pt C) {
        Vec PA = V(P, A), PQ = V(P, Q), PB = V(P, B), PC = V(P, C), QA = V(Q, A), QB = V(Q, B), QC = V(Q, C);
        boolean p = cw(PA, PB, PC), q = cw(QA, QB, QC), a = cw(PQ, PB, PC), b = cw(PA, PQ, PC), c = cw(PQ, PB, PQ);
        return (p != q) && (p == a) && (p == b) && (p == c);
    }

    public static float rayParameterToIntersection(Pt E, Vec T, Pt A, Pt B, Pt C) {
        Vec AE = V(A, E), AB = V(A, B), AC = V(A, C);
        return -m(AE, AC, AB) / m(T, AC, AB);
    }

}
