package tube;

import spray.Geometry.Vec3;

import static spray.Geometry.*;

// point
public class Pt {

    public Vec3 $ = origin3();

    public Pt() {
    }

    public Pt(Vec3 $) {
        this.$ = $;
    }

    public Pt(double px, double py, double pz) {
        $ = xyz(px, py, pz);
    }

    public Pt set(float px, float py, float pz) {
        $ = xyz(px, py, pz);
        return this;
    }

    public Pt set(Pt P) {
        $ = P.$;
        return this;
    }

    public Pt add(float s, Vec V) {
        $ = $.add(V.$.mult(s));
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

    // A+sAB
    static Pt P(Pt A, float s, Pt B) {
        Vec3 AB = B.$.sub(A.$);
        return new Pt(A.$.add(AB.mult(s)));
    }

    // (A+B)/2
    static Pt P(Pt A, Pt B) {
        return new Pt(A.$.add(B.$).div(2));
    }

    // sA
    static Pt P(float s, Pt A) {
        return new Pt(A.$.mult(s));
    }

    // A+B
    static Pt A(Pt A, Pt B) {
        return new Pt(A.$.add(B.$));
    }

    // aA+bB
    static Pt P(float a, Pt A, float b, Pt B) {
        return A(P(a, A), P(b, B));
    }

    // P+sV
    static Pt P(Pt P, float s, Vec V) {
        return new Pt(P.$.add(V.$.mult(s)));
    }

    // O+xI+yJ
    static Pt P(Pt O, float x, Vec I, float y, Vec J) {
        return new Pt(
            O.$
                .add(I.$.mult(x))
                .add(J.$.mult(y))
        );
    }

    static void makePts(Pt[] C) {
        for (int i = 0; i < C.length; i++) C[i] = P();
    }

}
