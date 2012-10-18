package tube;

import static processing.core.PApplet.*;

import static tube.Measures.*;

// vector
public class Vec {

    public float x, y, z;

    public Vec() {
    }

    public Vec(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
    }

    public Vec set(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
        return this;
    }

    public Vec set(Vec V) {
        x = V.x;
        y = V.y;
        z = V.z;
        return this;
    }

    public Vec add(Vec V) {
        x += V.x;
        y += V.y;
        z += V.z;
        return this;
    }

    public Vec add(float s, Vec V) {
        x += s * V.x;
        y += s * V.y;
        z += s * V.z;
        return this;
    }

    public Vec sub(Vec V) {
        x -= V.x;
        y -= V.y;
        z -= V.z;
        return this;
    }

    public Vec mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public Vec div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public Vec div(int f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public Vec rev() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public float norm() {
        return (sqrt(sq(x) + sq(y) + sq(z)));
    }

    public Vec normalize() {
        float n = norm();
        if (n > 0.000001) {
            div(n);
        }
        return this;
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
    static Vec V(float x, float y, float z) {
        return new Vec(x, y, z);
    }

    // make copy of vector V
    static Vec V(Vec V) {
        return new Vec(V.x, V.y, V.z);
    }

    // A+B
    static Vec A(Vec A, Vec B) {
        return new Vec(A.x + B.x, A.y + B.y, A.z + B.z);
    }

    // U+sV
    static Vec A(Vec U, float s, Vec V) {
        return V(U.x + s * V.x, U.y + s * V.y, U.z + s * V.z);
    }

    // U-V
    static Vec M(Vec U, Vec V) {
        return V(U.x - V.x, U.y - V.y, U.z - V.z);
    }

    // (A+B)/2
    static Vec V(Vec A, Vec B) {
        return new Vec((A.x + B.x) / 2.0f, (A.y + B.y) / 2.0f, (A.z + B.z) / 2.0f);
    }

    // (1-s)A+sB
    static Vec V(Vec A, float s, Vec B) {
        return new Vec(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y), A.z + s * (B.z - A.z));
    }

    // (A+B+C)/3
    static Vec V(Vec A, Vec B, Vec C) {
        return new Vec((A.x + B.x + C.x) / 3.0f, (A.y + B.y + C.y) / 3.0f, (A.z + B.z + C.z) / 3.0f);
    }

    // (A+B+C+D)/4
    static Vec V(Vec A, Vec B, Vec C, Vec D) {
        return V(V(A, B), V(C, D));
    }

    // sA
    static Vec V(float s, Vec A) {
        return new Vec(s * A.x, s * A.y, s * A.z);
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
        return new Vec(Q.x - P.x, Q.y - P.y, Q.z - P.z);
    }

    // V/||V||
    static Vec U(Vec V) {
        float n = V.norm();
        if (n < 0.000001) return V(0, 0, 0);
        else return V.div(n);
    }

    // UxV cross product (normal to both)
    static Vec N(Vec U, Vec V) {
        return V(U.y * V.z - U.z * V.y, U.z * V.x - U.x * V.z, U.x * V.y - U.y * V.x);
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
