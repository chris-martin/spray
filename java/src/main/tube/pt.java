package tube;

// point
public class pt {

    public float x, y, z;

    public pt() {
    }

    public pt(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
    }

    public pt set(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
        return this;
    }

    public pt set(pt P) {
        x = P.x;
        y = P.y;
        z = P.z;
        return this;
    }

    public pt add(pt P) {
        x += P.x;
        y += P.y;
        z += P.z;
        return this;
    }

    public pt add(vec V) {
        x += V.x;
        y += V.y;
        z += V.z;
        return this;
    }

    public pt add(float s, vec V) {
        x += s * V.x;
        y += s * V.y;
        z += s * V.z;
        return this;
    }

    public pt sub(pt P) {
        x -= P.x;
        y -= P.y;
        z -= P.z;
        return this;
    }

    public pt mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public pt div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public pt div(int f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    // point (x,y,z)
    static pt P() {
        return new pt();
    }

    // point (x,y,z)
    static pt P(float x, float y, float z) {
        return new pt(x, y, z);
    }

    // copy of point P
    static pt P(pt A) {
        return new pt(A.x, A.y, A.z);
    }

    // A+sAB
    static pt P(pt A, float s, pt B) {
        return new pt(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y), A.z + s * (B.z - A.z));
    }

    // (A+B)/2
    static pt P(pt A, pt B) {
        return P((A.x + B.x) / 2.0f, (A.y + B.y) / 2.0f, (A.z + B.z) / 2.0f);
    }

    // (A+B+C)/3
    static pt P(pt A, pt B, pt C) {
        return new pt((A.x + B.x + C.x) / 3.0f, (A.y + B.y + C.y) / 3.0f, (A.z + B.z + C.z) / 3.0f);
    }

    // (A+B+C+D)/4
    static pt P(pt A, pt B, pt C, pt D) {
        return P(P(A, B), P(C, D));
    }

    // sA
    static pt P(float s, pt A) {
        return new pt(s * A.x, s * A.y, s * A.z);
    }

    // A+B
    static pt A(pt A, pt B) {
        return new pt(A.x + B.x, A.y + B.y, A.z + B.z);
    }

    // aA+bB
    static pt P(float a, pt A, float b, pt B) {
        return A(P(a, A), P(b, B));
    }

    // aA+bB+cC
    static pt P(float a, pt A, float b, pt B, float c, pt C) {
        return A(P(a, A), P(b, B, c, C));
    }

    // aA+bB+cC+dD
    static pt P(float a, pt A, float b, pt B, float c, pt C, float d, pt D) {
        return A(P(a, A, b, B), P(c, C, d, D));
    }

    // P+V
    static pt P(pt P, vec V) {
        return new pt(P.x + V.x, P.y + V.y, P.z + V.z);
    }

    // P+sV
    static pt P(pt P, float s, vec V) {
        return new pt(P.x + s * V.x, P.y + s * V.y, P.z + s * V.z);
    }

    // O+xI+yJ
    static pt P(pt O, float x, vec I, float y, vec J) {
        return P(O.x + x * I.x + y * J.x, O.y + x * I.y + y * J.y, O.z + x * I.z + y * J.z);
    }

    // O+xI+yJ+kZ
    static pt P(pt O, float x, vec I, float y, vec J, float z, vec K) {
        return P(O.x + x * I.x + y * J.x + z * K.x, O.y + x * I.y + y * J.y + z * K.y, O.z + x * I.z + y * J.z + z * K.z);
    }

    static void makePts(pt[] C) {
        for (int i = 0; i < C.length; i++) C[i] = P();
    }

}
