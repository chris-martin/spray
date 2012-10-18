package tube;

import static processing.core.PApplet.*;

// vector
public class vec {

    public float x, y, z;

    public vec() {
    }

    public vec(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
    }

    public vec set(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
        return this;
    }

    public vec set(vec V) {
        x = V.x;
        y = V.y;
        z = V.z;
        return this;
    }

    public vec add(vec V) {
        x += V.x;
        y += V.y;
        z += V.z;
        return this;
    }

    public vec add(float s, vec V) {
        x += s * V.x;
        y += s * V.y;
        z += s * V.z;
        return this;
    }

    public vec sub(vec V) {
        x -= V.x;
        y -= V.y;
        z -= V.z;
        return this;
    }

    public vec mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public vec div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public vec div(int f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public vec rev() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public float norm() {
        return (sqrt(sq(x) + sq(y) + sq(z)));
    }

    public vec normalize() {
        float n = norm();
        if (n > 0.000001) {
            div(n);
        }
        return this;
    }

    // Rotate by a parallel to plane (I,J)
    public vec rotate(float a, vec I, vec J) {
        float x = Main.d(this, I), y = Main.d(this, J);
        float c = cos(a), s = sin(a);
        add(x * c - x - y * s, I);
        add(x * s + y * c - y, J);
        return this;
    }

    // make vector (x,y,z)
    static vec V() {
        return new vec();
    }

    // make vector (x,y,z)
    static vec V(float x, float y, float z) {
        return new vec(x, y, z);
    }

    // make copy of vector V
    static vec V(vec V) {
        return new vec(V.x, V.y, V.z);
    }

    // A+B
    static vec A(vec A, vec B) {
        return new vec(A.x + B.x, A.y + B.y, A.z + B.z);
    }

    // U+sV
    static vec A(vec U, float s, vec V) {
        return V(U.x + s * V.x, U.y + s * V.y, U.z + s * V.z);
    }

    // U-V
    static vec M(vec U, vec V) {
        return V(U.x - V.x, U.y - V.y, U.z - V.z);
    }

    // (A+B)/2
    static vec V(vec A, vec B) {
        return new vec((A.x + B.x) / 2.0f, (A.y + B.y) / 2.0f, (A.z + B.z) / 2.0f);
    }

    // (1-s)A+sB
    static vec V(vec A, float s, vec B) {
        return new vec(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y), A.z + s * (B.z - A.z));
    }

    // (A+B+C)/3
    static vec V(vec A, vec B, vec C) {
        return new vec((A.x + B.x + C.x) / 3.0f, (A.y + B.y + C.y) / 3.0f, (A.z + B.z + C.z) / 3.0f);
    }

    // (A+B+C+D)/4
    static vec V(vec A, vec B, vec C, vec D) {
        return V(V(A, B), V(C, D));
    }

    // sA
    static vec V(float s, vec A) {
        return new vec(s * A.x, s * A.y, s * A.z);
    }

    // aA+bB
    static vec V(float a, vec A, float b, vec B) {
        return A(V(a, A), V(b, B));
    }

    // aA+bB+cC
    static vec V(float a, vec A, float b, vec B, float c, vec C) {
        return A(V(a, A, b, B), V(c, C));
    }

    // PQ
    static vec V(pt P, pt Q) {
        return new vec(Q.x - P.x, Q.y - P.y, Q.z - P.z);
    }

    // V/||V||
    static vec U(vec V) {
        float n = V.norm();
        if (n < 0.000001) return V(0, 0, 0);
        else return V.div(n);
    }

    // UxV cross product (normal to both)
    static vec N(vec U, vec V) {
        return V(U.y * V.z - U.z * V.y, U.z * V.x - U.x * V.z, U.x * V.y - U.y * V.x);
    }

    // normal to triangle (A,B,C), not normalized (proportional to area)
    static vec N(pt A, pt B, pt C) {
        return N(V(A, B), V(A, C));
    }

    // (UxV)xV unit normal to U in the plane UV
    static vec B(vec U, vec V) {
        return U(N(N(U, V), U));
    }

}
