package tube;

import spray.Geometry.Vec3;

import static processing.core.PApplet.*;

import static spray.Geometry.*;
import static tube.Measures.*;

// vector
public class Vec {

    public Vec3 $ = origin3();

    public Vec() {
    }

    public Vec(Vec3 $) {
        this.$ = $;
    }

    public Vec(double px, double py, double pz) {
        $ = xyz(px, py, pz);
    }

    public Vec set(float px, float py, float pz) {
        $ = xyz(px, py, pz);
        return this;
    }

    public Vec set(Vec V) {
        $ = V.$;
        return this;
    }

    public Vec add(float s, Vec V) {
        $ = $.add(V.$.mult(s));
        return this;
    }

    public Vec div(float f) {
        $ = $.div(f);
        return this;
    }

    public float norm() {
        return (float) $.mag();
    }

    // Rotate by a parallel to plane (I,J)
    public Vec rotate(float a, Vec I, Vec J) {
        float x = d(this, I), y = d(this, J);
        float c = cos(a), s = sin(a);
        add(x * c - x - y * s, I);
        add(x * s + y * c - y, J);
        return this;
    }

    // make vector (x,y,z)
    static Vec V() {
        return new Vec();
    }

    // make vector (x,y,z)
    static Vec V(double x, double y, double z) {
        return new Vec(x, y, z);
    }

    // make copy of vector V
    static Vec V(Vec V) {
        return new Vec(V.$);
    }

    // A+B
    static Vec A(Vec A, Vec B) {
        return new Vec(A.$.add(B.$));
    }

    // U+sV
    static Vec A(Vec U, float s, Vec V) {
        return new Vec(U.$.add(V.$.mult(s)));
    }

    // U-V
    static Vec M(Vec U, Vec V) {
        return new Vec(U.$.sub(V.$));
    }

    // (A+B)/2
    static Vec V(Vec A, Vec B) {
        return new Vec(A.$.add(B.$).div(2));
    }

    // sA
    static Vec V(float s, Vec A) {
        return new Vec(A.$.mult(s));
    }

    // aA+bB
    static Vec V(float a, Vec A, float b, Vec B) {
        return A(V(a, A), V(b, B));
    }

    // aA+bB+cC
    static Vec V(float a, Vec A, float b, Vec B, float c, Vec C) {
        return A(V(a, A, b, B), V(c, C));
    }

    // PQ
    static Vec V(Pt P, Pt Q) {
        return new Vec(Q.$.sub(P.$));
    }

    // V/||V||
    static Vec U(Vec V) {
        float n = V.norm();
        if (n < 0.000001) return V(0, 0, 0);
        else return V.div(n);
    }

    // UxV cross product (normal to both)
    static Vec N(Vec U, Vec V) {
        return new Vec(U.$.cross(V.$));
    }

    // normal to triangle (A,B,C), not normalized (proportional to area)
    static Vec N(Pt A, Pt B, Pt C) {
        return N(V(A, B), V(A, C));
    }

    // (UxV)xV unit normal to U in the plane UV
    static Vec B(Vec U, Vec V) {
        return U(N(N(U, V), U));
    }

}
