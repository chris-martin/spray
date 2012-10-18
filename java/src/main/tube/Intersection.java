package tube;

import spray.Geometry.Line3;
import spray.Geometry.Vec3;

import static spray.Geometry.aToB;
import static tube.Measures.cw;
import static tube.Measures.m;

public class Intersection {

    // if (P,Q) intersects (A,B,C), return the intersection point
    public static Vec3 intersect(Line3 PQ, Vec3 A, Vec3 B, Vec3 C) {
        return intersect(PQ.a(), PQ.ab(), A, B, C);
    }

    // if ray from E along T intersects triangle (A,B,C), return the intersection point
    public static Vec3 intersect(Vec3 E, Vec3 T, Vec3 A, Vec3 B, Vec3 C) {
        Vec3 EA = aToB(E, A).ab(), EB = aToB(E, B).ab(), EC = aToB(E, C).ab(), AB = aToB(A, B).ab(), AC = aToB(A, C).ab();
        boolean s = cw(EA, EB, EC), sA = cw(T, EB, EC), sB = cw(EA, T, EC), sC = cw(EA, EB, T);
        if ((s == sA) && (s == sB) && (s == sC)) {
            return null;
        }
        float t = m(EA, AC, AB) / m(T, AC, AB);
        return E.add(T.mult(t));
    }

    // true if ray from E with direction T hits triangle (A,B,C)
    public static boolean rayIntersectsTriangle(Vec3 E, Vec3 T, Vec3 A, Vec3 B, Vec3 C) {
        Vec3 EA = aToB(E, A).ab(), EB = aToB(E, B).ab(), EC = aToB(E, C).ab();
        boolean s = cw(EA, EB, EC), sA = cw(T, EB, EC), sB = cw(EA, T, EC), sC = cw(EA, EB, T);
        return (s == sA) && (s == sB) && (s == sC);
    }

    public static boolean edgeIntersectsTriangle(Vec3 P, Vec3 Q, Vec3 A, Vec3 B, Vec3 C) {
        Vec3 PA = aToB(P, A).ab(), PQ = aToB(P, Q).ab(), PB = aToB(P, B).ab(), PC = aToB(P, C).ab(),
            QA = aToB(Q, A).ab(), QB = aToB(Q, B).ab(), QC = aToB(Q, C).ab();
        boolean p = cw(PA, PB, PC), q = cw(QA, QB, QC), a = cw(PQ, PB, PC), b = cw(PA, PQ, PC), c = cw(PQ, PB, PQ);
        return (p != q) && (p == a) && (p == b) && (p == c);
    }

    public static float rayParameterToIntersection(Vec3 E, Vec3 T, Vec3 A, Vec3 B, Vec3 C) {
        Vec3 AE = aToB(A, E).ab(), AB = aToB(A, B).ab(), AC = aToB(A, C).ab();
        return -m(AE, AC, AB) / m(T, AC, AB);
    }

}
