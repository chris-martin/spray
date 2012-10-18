package tube;

import spray.Geometry.Vec3;

import static processing.core.PApplet.*;
import static spray.Geometry.aToB;

public class Measures {

    // (UxV)*W  mixed product, determinant
    public static float m(Vec3 U, Vec3 V, Vec3 W) {
        return U.dot(V.cross(W));
    }

    // det (EA EB EC) is >0 when E sees (A,B,C) clockwise
    public static float m(Vec3 E, Vec3 A, Vec3 B, Vec3 C) {
        return m(A.sub(E), B.sub(E), C.sub(E));
    }

    // area of triangle
    public static float area(Vec3 A, Vec3 B, Vec3 C) {
        return N(A, B, C).mag() / 2;
    }

    // volume of tet
    public static float volume(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
        return m(B.sub(A), C.sub(A), D.sub(A)) / 6;
    }

    // true if U and V are almost parallel
    public static boolean parallel(Vec3 U, Vec3 V) {
        return U.cross(V).mag() < U.mag() * V.mag() * 0.00001;
    }

    // angle(U,V)
    public static float angle(Vec3 U, Vec3 V) {
        return acos(U.dot(V) / V.mag() / U.mag());
    }

    // (UxV)*W>0  U,V,W are clockwise
    public static boolean cw(Vec3 U, Vec3 V, Vec3 W) {
        return m(U, V, W) > 0;
    }

    // tet is oriented so that A sees B, C, D clockwise
    public static boolean cw(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
        return volume(A, B, C, D) > 0;
    }

    // normal to triangle (A,B,C), not normalized (proportional to area)
    static Vec3 N(Vec3 A, Vec3 B, Vec3 C) {
        Vec3 AB = aToB(A, B).ab(), AC = aToB(A, C).ab();
        return AB.cross(AC);
    }

}
