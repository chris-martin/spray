package tube;

import spray.Geometry.Vec3;

import static processing.core.PApplet.*;
import static tube.Vec.N;

public class Measures {

    //U*V dot product
    public static float d(Vec U, Vec V) {
        return U.$.x() * V.$.x() + U.$.y() * V.$.y() + U.$.z() * V.$.z();
    }

    // (UxV)*W  mixed product, determinant
    public static float m(Vec3 U, Vec3 V, Vec3 W) {
        return U.dot(V.cross(W));
    }

    // det (EA EB EC) is >0 when E sees (A,B,C) clockwise
    public static float m(Vec3 E, Vec3 A, Vec3 B, Vec3 C) {
        return m(A.sub(E), B.sub(E), C.sub(E));
    }

    // V*V    norm squared
    public static float n2(Vec V) {
        return sq(V.$.x()) + sq(V.$.y()) + sq(V.$.z());
    }

    // ||V||  norm
    public static float n(Vec V) {
        return sqrt(n2(V));
    }

    // ||AB|| distance
    public static float d(Pt P, Pt Q) {
        return sqrt(sq(Q.$.x() - P.$.x()) + sq(Q.$.y() - P.$.y()) + sq(Q.$.z() - P.$.z()));
    }

    // area of triangle
    public static float area(Pt A, Pt B, Pt C) {
        return n(N(A, B, C)) / 2;
    }

    // volume of tet
    public static float volume(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
        return m(B.sub(A), C.sub(A), D.sub(A)) / 6;
    }

    // true if U and V are almost parallel
    public static boolean parallel(Vec U, Vec V) {
        return n(N(U, V)) < n(U) * n(V) * 0.00001;
    }

    // angle(U,V)
    public static float angle(Vec U, Vec V) {
        return acos(d(U, V) / n(V) / n(U));
    }

    // (UxV)*W>0  U,V,W are clockwise
    public static boolean cw(Vec3 U, Vec3 V, Vec3 W) {
        return m(U, V, W) > 0;
    }

    // tet is oriented so that A sees B, C, D clockwise
    public static boolean cw(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
        return volume(A, B, C, D) > 0;
    }

}
