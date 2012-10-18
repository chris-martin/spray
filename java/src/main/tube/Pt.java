package tube;

// point
public class Pt {

    public float x, y, z;

    public Pt() {
    }

    public Pt(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
    }

    public Pt set(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
        return this;
    }

    public Pt set(Pt P) {
        x = P.x;
        y = P.y;
        z = P.z;
        return this;
    }

    public Pt add(Pt P) {
        x += P.x;
        y += P.y;
        z += P.z;
        return this;
    }

    public Pt add(Vec V) {
        x += V.x;
        y += V.y;
        z += V.z;
        return this;
    }

    public Pt add(float s, Vec V) {
        x += s * V.x;
        y += s * V.y;
        z += s * V.z;
        return this;
    }

    public Pt sub(Pt P) {
        x -= P.x;
        y -= P.y;
        z -= P.z;
        return this;
    }

    public Pt mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public Pt div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public Pt div(int f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    // point (x,y,z)
    static Pt P() {
        return new Pt();
    }

    // point (x,y,z)
    static Pt P(float x, float y, float z) {
        return new Pt(x, y, z);
    }

    // copy of point P
    static Pt P(Pt A) {
        return new Pt(A.x, A.y, A.z);
    }

    // A+sAB
    static Pt P(Pt A, float s, Pt B) {
        return new Pt(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y), A.z + s * (B.z - A.z));
    }

    // (A+B)/2
    static Pt P(Pt A, Pt B) {
        return P((A.x + B.x) / 2.0f, (A.y + B.y) / 2.0f, (A.z + B.z) / 2.0f);
    }

    // (A+B+C)/3
    static Pt P(Pt A, Pt B, Pt C) {
        return new Pt((A.x + B.x + C.x) / 3.0f, (A.y + B.y + C.y) / 3.0f, (A.z + B.z + C.z) / 3.0f);
    }

    // (A+B+C+D)/4
    static Pt P(Pt A, Pt B, Pt C, Pt D) {
        return P(P(A, B), P(C, D));
    }

    // sA
    static Pt P(float s, Pt A) {
        return new Pt(s * A.x, s * A.y, s * A.z);
    }

    // A+B
    static Pt A(Pt A, Pt B) {
        return new Pt(A.x + B.x, A.y + B.y, A.z + B.z);
    }

    // aA+bB
    static Pt P(float a, Pt A, float b, Pt B) {
        return A(P(a, A), P(b, B));
    }

    // aA+bB+cC
    static Pt P(float a, Pt A, float b, Pt B, float c, Pt C) {
        return A(P(a, A), P(b, B, c, C));
    }

    // aA+bB+cC+dD
    static Pt P(float a, Pt A, float b, Pt B, float c, Pt C, float d, Pt D) {
        return A(P(a, A, b, B), P(c, C, d, D));
    }

    // P+V
    static Pt P(Pt P, Vec V) {
        return new Pt(P.x + V.x, P.y + V.y, P.z + V.z);
    }

    // P+sV
    static Pt P(Pt P, float s, Vec V) {
        return new Pt(P.x + s * V.x, P.y + s * V.y, P.z + s * V.z);
    }

    // O+xI+yJ
    static Pt P(Pt O, float x, Vec I, float y, Vec J) {
        return P(O.x + x * I.x + y * J.x, O.y + x * I.y + y * J.y, O.z + x * I.z + y * J.z);
    }

    // O+xI+yJ+kZ
    static Pt P(Pt O, float x, Vec I, float y, Vec J, float z, Vec K) {
        return P(O.x + x * I.x + y * J.x + z * K.x, O.y + x * I.y + y * J.y + z * K.y, O.z + x * I.z + y * J.z + z * K.z);
    }

    static void makePts(Pt[] C) {
        for (int i = 0; i < C.length; i++) C[i] = P();
    }

}
