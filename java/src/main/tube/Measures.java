package tube;

import static tube.Vec.N;
import static tube.Vec.V;

import static processing.core.PApplet.*;

public class Measures {

    //U*V dot product
    public static float d(Vec U, Vec V) {
        return U.x * V.x + U.y * V.y + U.z * V.z;
    }

    // (UxV)*W  mixed product, determinant
    public static float m(Vec U, Vec V, Vec W) {
        return d(U, N(V, W));
    }

    // det (EA EB EC) is >0 when E sees (A,B,C) clockwise
    public static float m(Pt E, Pt A, Pt B, Pt C) {
        return m(V(E, A), V(E, B), V(E, C));
    }

    // V*V    norm squared
    public static float n2(Vec V) {
        return sq(V.x) + sq(V.y) + sq(V.z);
    }

    // ||V||  norm
    public static float n(Vec V) {
        return sqrt(n2(V));
    }

    // ||AB|| distance
    public static float d(Pt P, Pt Q) {
        return sqrt(sq(Q.x - P.x) + sq(Q.y - P.y) + sq(Q.z - P.z));
    }

    // area of triangle
    public static float area(Pt A, Pt B, Pt C) {
        return n(N(A, B, C)) / 2;
    }

    // volume of tet
    public static float volume(Pt A, Pt B, Pt C, Pt D) {
        return m(V(A, B), V(A, C), V(A, D)) / 6;
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
    public static boolean cw(Vec U, Vec V, Vec W) {
        return m(U, V, W) > 0;
    }

    // tet is oriented so that A sees B, C, D clockwise
    public static boolean cw(Pt A, Pt B, Pt C, Pt D) {
        return volume(A, B, C, D) > 0;
    }

}
