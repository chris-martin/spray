package tube;

import processing.core.PApplet;
import processing.opengl.*;

import spray.Geometry.Vec3;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.nio.*;

import static spray.Geometry.*;
import static tube.Color.*;
import static tube.Measures.*;
import static tube.Rotate.*;

//*********************************************************************
//**      3D viewer with camera control and surface picking          **
//**              Jarek Rossignac, October 2010                      **
//**                    (using PVectors)                             **
//*********************************************************************

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[]{"tube.Main"});
    }

    GL gl;
    GLU glu;

    public void setup() {
        size(900, 500, OPENGL);
        sphereDetail(12);
        rectMode(CENTER);
        glu = ((PGraphicsOpenGL) g).glu;
        PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
        gl = pgl.beginGL();
        pgl.endGL();
        initm();
    }

    public void draw() {
        background(black);
        changeViewAndFrame();

        // displays surface of model 1 to set the z-buffer for picking points
        // Q and T and for picking the local frames 1 and 2
        fill(yellow);
        pushMatrix();
        rotateY(PI / 2);
        rect(0, 0, 400, 400);
        popMatrix();

        // sets the target point T where the mouse points.
        // The camera will turn toward's it when the 't' key is released
        if (keyPressed && key == 't') {
            T = Pick();
        }

        // sets Q to the picked surface-point and {I,J,K} to screen aligned vectors
        if (keyPressed && key == 'q') {
            SetFrameFromPick();
        }

        // the following 2 actions set frame (1 or 2) and edit its origin as the mouse is dragged
        if (keyPressed && key == '1') {
            m = 1;
            SetFrameFromPick();
            mQ[m] = Q;
            if (first[m]) {
                mI[m] = I;
                mJ[m] = J;
                mK[m] = K;
                first[m] = false;
            }
        }
        if (keyPressed && key == '2') {
            m = 2;
            SetFrameFromPick();
            mQ[m] = Q;
            if (first[m]) {
                mI[m] = I;
                mJ[m] = J;
                mK[m] = K;
                first[m] = false;
            }
        }

        // to reset the z-buffer used above for picking
        // background(white);

        // shows local frame aligned with screen when picked ('q' pressed) using (R,G,B) lines
        noStroke();
        show(Q, 30, I, J, K);

        // shows picked point in cyan (it is set when 't' is pressed and becomes the focus)
        noStroke();
        fill(cyan);
        show(T, 2);

        // shows origin of frame 1 and 2 as a small red or green ball
        fill(red);
        show(mQ[1], 3);
        fill(green);
        show(mQ[2], 3);

        if (m == 1) {
            fill(red);
        } else if (m == 2) {
            fill(green);
        } else if (m == 0) {
            fill(blue);
        }

        // shows origin of selected frame (R,G,B) for (1,2,0) as a bigger ball
        show(mQ[m], 5);

        // shows current point on surface. Changed when 'q' is pressed
        fill(yellow);
        show(Q, 2);

        // shows second model (currently axes)
        noStroke();
        showModel();

        // showTube(mQ[1],mK[1],mQ[2],mK[2],10);
        showQuads(mQ[1], mK[1], mJ[1], mQ[2], mK[2], mJ[2], 20, 20, 20, green);

    }

    // ****************** INTERRUPTS *************************

    // camera rotation around T when no key is pressed
    public void mouseDragged() {
        if (keyPressed) {
            return;
        }
        a -= PI * (mouseY - pmouseY) / height;
        a = max(-PI / 2 + 0.1f, a);
        a = min(PI / 2 - 0.1f, a);
        b += PI * (mouseX - pmouseX) / width;
    }

    // sets the new focus point to wher ethe mous points to when the mouse-button is released
    public void keyReleased() {
        if (key == 't') {
            L = T;
        }
    }

    public void keyPressed() {

        // toggles shaded versus wireframe viewing of the first model
        if (key == 'm') showModel = !showModel;

        // reset the view
        if (key == ' ') {
            d = 300;
            b = 0;
            a = 0;
            L = origin3();
        }

        // reset the current frame to be defined by the mouse position and the screen orientation
        if (key == 'h') {
            mQ[m] = Q;
            mI[m] = I;
            mJ[m] = J;
            mK[m] = K;
        }
    }

    // ************************ Graphic pick utilities *******************************

    // camera target point set with mouse when pressing 't'
    Vec3 T = origin3();

    // eye and lookAt
    Vec3 E = origin3(), L = origin3();

    Vec3[] mQ = new Vec3[3];

    // three local frames {Q,I,J,K}
    Vec3[] mI = new Vec3[3], mJ = new Vec3[3], mK = new Vec3[3];

    // which frame is being shown / edited
    int m = 1;

    // view parameters: distance, angles, q
    float d = 300, b = 0, a = 0;

    Boolean showModel = true;

    // track if the frame has already been set
    Boolean[] first = {true, true, true};

    // picked surface point Q and screen aligned vectors {I,J,K} set when picked
    Vec3 Q = origin3();
    Vec3 I = origin3(), J = origin3(), K = origin3();

    // declares the local frames
    void initm() {
        for (int i = 0; i < 3; i++) {
            mQ[i] = origin3();
            mI[i] = origin3();
            mJ[i] = origin3();
            mK[i] = origin3();
        }
    }

    void changeViewAndFrame() {

        // viewing direction angles
        float ca = cos(a), sa = sin(a), cb = cos(b), sb = sin(b);

        // moves the selected frame parallel to the screen
        if (keyPressed && key == 'x') {
            mQ[m] = mQ[m].add(I.mult(mouseX - pmouseX)).add(J.mult(pmouseY - mouseY));
        }

        // moves the selected frame on th ehorizontal plane
        if (keyPressed && key == 'z') {
            mQ[m] = mQ[m].add(I).mult(mouseX - pmouseX).add(K.mult(mouseY - pmouseY));
        }

        // changes distance form the target to the viewpoint
        if (keyPressed && key == 'd') {
            d -= mouseY - pmouseY;
        }

        // rotates current frame parallel to the screen
        if (keyPressed && key == 'a') {
            float a = (float) (-mouseY + pmouseY + mouseX - pmouseX) / width;
            mI[m] = R(mI[m], a, I, J);
            mJ[m] = R(mJ[m], a, I, J);
            mK[m] = R(mK[m], a, I, J);
        }

        // rotates the current frames in pitch and yaw
        if (keyPressed && key == 'r') {

            float a = (float) (mouseY - pmouseY) / width;
            mI[m] = R(mI[m], a, J, K);
            mJ[m] = R(mJ[m], a, J, K);
            mK[m] = R(mK[m], a, J, K);

            float b = (float) (pmouseX - mouseX) / width;
            mI[m] = R(mI[m], b, I, K);
            mJ[m] = R(mJ[m], b, I, K);
            mK[m] = R(mK[m], b, I, K);
        }

        // sets the eye
        E = xyz(d * cb * ca, d * sa, d * sb * ca);

        // defines the view : eye, ctr, up
        camera(E.x(), E.y(), E.z(), L.x(), L.y(), L.z(), 0.0f, 1.0f, 0.0f);

        // puts a white light above and to the left of the viewer
        directionalLight(250, 250, 250, -E.x(), -E.y() + 100, -E.z());

        // in case you want the light to be fixed in model space
        // ambientLight(100,100,0);
        // directionalLight(250, 250, 0, -20, 10, 5);
        // directionalLight(100, 100, 250, 10, -20, -5);
    }

    // sets Q where the mouse points to and I, J, K to be aligned with the screen
    // (I right, J up, K towards the viewer)
    // Pt Q, Vec I, Vec J, Vec K
    void SetFrameFromPick() {
        glu = ((PGraphicsOpenGL) g).glu;
        PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
        float modelviewm[] = new float[16];
        gl = pgl.beginGL();
        gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, modelviewm, 0);
        pgl.endGL();
        Q = Pick();
        I = xyz(modelviewm[0], modelviewm[4], modelviewm[8]);
        J = xyz(modelviewm[1], modelviewm[5], modelviewm[9]);
        K = xyz(modelviewm[2], modelviewm[6], modelviewm[10]);
    }

    Vec3 Pick() {
        ((PGraphicsOpenGL) g).beginGL();
        int viewport[] = new int[4];
        double[] proj = new double[16];
        double[] model = new double[16];
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, model, 0);
        FloatBuffer fb = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        gl.glReadPixels(mouseX, height - mouseY, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fb);
        fb.rewind();
        double[] mousePosArr = new double[4];
        glu.gluUnProject(
            (double) mouseX, height - (double) mouseY, (double) fb.get(0),
            model, 0, proj, 0, viewport, 0, mousePosArr, 0);
        ((PGraphicsOpenGL) g).endGL();
        return xyz((float) mousePosArr[0], (float) mousePosArr[1], (float) mousePosArr[2]);
    }

    // ********************** display utilities ****************************

    // sets the matrix and displays the second model (here the axes as blocks)
    void showModel() {
        pushMatrix();
        applyMatrix(
            mI[m].x(), mJ[m].x(), mK[m].x(), mQ[m].x(),
            mI[m].y(), mJ[m].y(), mK[m].y(), mQ[m].y(),
            mI[m].z(), mJ[m].z(), mK[m].z(), mQ[m].z(),
            0.0f, 0.0f, 0.0f, 1.0f);

        // replace this (showing the axes) with code for showing your second model
        showAxes(30);
        popMatrix();
    }

    // shows three orthogonal axes as red, green, blue blocks aligned with the local frame coordinates
    void showAxes(float s) {
        noStroke();
        pushMatrix();
        pushMatrix();
        fill(red);
        scale(s, 1, 1);
        box(2);
        popMatrix();
        pushMatrix();
        fill(green);
        scale(1, s, 1);
        box(2);
        popMatrix();
        pushMatrix();
        fill(blue);
        scale(1, 1, s);
        box(2);
        popMatrix();
        popMatrix();
    }

    //*********************************************************************
    //**                      3D geeomtry tools                          **
    //**              Jarek Rossignac, October 2010                      **
    //**                                                                 **
    //*********************************************************************

    // mouse

    // current mouse location
    Vec3 Mouse() {
        return xyz(mouseX, mouseY, 0);
    }

    // vector representing recent mouse displacement
    Vec3 MouseDrag() {
        return xyz(mouseX - pmouseX, mouseY - pmouseY, 0);
    }

    // render

    // changes normal for smooth shading
    void normal(Vec3 V) {
        normal(V.x(), V.y(), V.z());
    }

    // vertex for shading or drawing
    void v(Vec3 P) {
        vertex(P.x(), P.y(), P.z());
    }

    // vertex with texture coordinates
    void vTextured(Vec3 P, float u, float v) {
        vertex(P.x(), P.y(), P.z(), u, v);
    }

    // draws edge (P,Q)
    void showEdgeFromPtoQ(Vec3 P, Vec3 Q) {
        line(Q.x(), Q.y(), Q.z(), P.x(), P.y(), P.z());
    }

    // shows edge from P to P+V
    void showEdgeFromPToOffset(Vec3 P, Vec3 V) {
        line(
            P.x(),
            P.y(),
            P.z(),
            P.x() + V.x(),
            P.y() + V.y(),
            P.z() + V.z()
        );
    }

    // shows edge from P to P+dV
    void showEdgeByPointAndOffset(Vec3 P, float d, Vec3 V) {
        line(
            P.x(),
            P.y(),
            P.z(),
            P.x() + d * V.x(),
            P.y() + d * V.y(),
            P.z() + d * V.z()
        );
    }

    // volume of tet
    void showTet(Vec3 A, Vec3 B, Vec3 C) {
        beginShape();
        v(A);
        v(B);
        v(C);
        endShape(CLOSE);
    }

    // volume of tet
    void showTet(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
        beginShape();
        v(A);
        v(B);
        v(C);
        v(D);
        endShape(CLOSE);
    }

    // render sphere of radius r and center P
    void show(Vec3 P, float r) {
        pushMatrix();
        translate(P.x(), P.y(), P.z());
        sphere(r);
        popMatrix();
    }

    // render sphere of radius r and center P
    // Pt P, float s, Vec I, Vec J, Vec K
    void show(Vec3 P, float s, Vec3 I, Vec3 J, Vec3 K) {
        noStroke();
        fill(yellow);
        show(P, 5);
        stroke(red);
        showEdgeByPointAndOffset(P, s, I);
        stroke(green);
        showEdgeByPointAndOffset(P, s, J);
        stroke(blue);
        showEdgeByPointAndOffset(P, s, K);
    }

    // prints string s in 3D at P
    void showText(Vec3 P, String s) {
        text(s, P.x(), P.y(), P.z());
    }

    // prints string s in 3D at P+D
    void showText(Vec3 P, String s, Vec3 D) {
        text(s, P.x() + D.x(), P.y() + D.y(), P.z() + D.z());
    }

    // curve

    // draws a cubic Bezier curve with control points A, B, C, D
    void bezier(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
        bezier(
            A.x(), A.y(), A.z(),
            B.x(), B.y(), B.z(),
            C.x(), C.y(), C.z(),
            D.x(), D.y(), D.z()
        );
    }

    // draws a cubic Bezier curve with control points A, B, C, D
    void bezier(Vec3[] C) {
        bezier(C[0], C[1], C[2], C[3]);
    }

    Vec3 bezierPoint(Vec3[] C, float t) {
        return xyz(
            bezierPoint(C[0].x(), C[1].x(), C[2].x(), C[3].x(), t),
            bezierPoint(C[0].y(), C[1].y(), C[2].y(), C[3].y(), t),
            bezierPoint(C[0].z(), C[1].z(), C[2].z(), C[3].z(), t)
        );
    }

    Vec3 bezierTangent(Vec3[] C, float t) {
        return xyz(
            bezierTangent(C[0].x(), C[1].x(), C[2].x(), C[3].x(), t),
            bezierTangent(C[0].y(), C[1].y(), C[2].y(), C[3].y(), t),
            bezierTangent(C[0].z(), C[1].z(), C[2].z(), C[3].z(), t)
        );
    }

    // draws cubic Bezier interpolating (P0,T0) and (P1,T1)
    // Pt P0, Vec T0, Pt P1, Vec T1
    void PT(Vec3 P0, Vec3 T0, Vec3 P1, Vec3 T1) {
        float d = distance(P0, P1) / 3;
        bezier(
            P0,
            P0.sub(T0.mag(d)),
            P1.sub(T1.mag(d)),
            P1
        );
    }

    // draws cubic Bezier interpolating (P0,T0) and (P1,T1)
    // Pt P0, Vec T0, Pt P1, Vec T1, Pt[] C
    void PTtoBezier(Vec3 P0, Vec3 T0, Vec3 P1, Vec3 T1, Vec3[] C) {
        float d = distance(P0, P1) / 3;
        C[0] = P0;
        C[1] = P0.sub(T0.mag(d));
        C[2] = P1.sub(T1.mag(d));
        C[3] = P1;
    }

    Vec3 vecToCubic(Vec3 A, Vec3 B, Vec3 C, Vec3 D, Vec3 E) {
        return xyz(
            (-A.x() + 4 * B.x() - 6 * C.x() + 4 * D.x() - E.x()) / 6,
            (-A.y() + 4 * B.y() - 6 * C.y() + 4 * D.y() - E.y()) / 6,
            (-A.z() + 4 * B.z() - 6 * C.z() + 4 * D.z() - E.z()) / 6
        );
    }

    Vec3 vecToProp(Vec3 B, Vec3 C, Vec3 D) {
        float cb = distance(C, B), cd = distance(C, D);
        float s = cb / (cb + cd);
        Vec3 BD = aToB(B, D).ab();
        return aToB(C, B.add(BD.mult(s))).ab();
    }

    // returns angle in 2D dragged by the mouse around the screen projection of G
    float angleDraggedAround(Vec3 G) {
        Vec3 S = xyz(screenX(G.x(), G.y(), G.z()), screenY(G.x(), G.y(), G.z()), 0);
        Vec3 T = aToB(S, Mouse()).ab();
        Vec3 U = aToB(S, Mouse()).ab();
        return atan2(U.rot90xy().dot(T), U.dot(T));
    }

    float scaleDraggedFrom(Vec3 G) {
        Vec3 S = xyz(screenX(G.x(), G.y(), G.z()), screenY(G.x(), G.y(), G.z()), 0);
        return S.dot(Mouse()) / S.dot(Mouse());
    }

    // TUBE
    // Pt P0, Vec T0, Pt P1, Vec T1
    void showTube(Vec3 P0, Vec3 T0, Vec3 P1, Vec3 T1, int n) {

        Vec3[] C = new Vec3[4];
        for (int i = 0; i < C.length; i++) {
            C[i] = origin3();
        }

        // shows an interpolating Bezier curve from frame 1 to frames 2
        // (tangents are defined by K vectors)
        stroke(cyan);
        noFill();
        PTtoBezier(P0, T0, P1, T1, C);
        bezier(C);
        noStroke();

        for (float t = 0; t <= 1; t += 0.1) {
            Vec3 B = bezierPoint(C, t);
            Vec3 T = bezierTangent(C, t);
            stroke(magenta);
            showEdgeByPointAndOffset(B, 0.1f, T);
            noStroke();
            fill(brown);
            show(B, 1);
        }
    }

    // Pt P0, Vec T0, Vec N0, Pt P1, Vec T1, Vec N1
    void showQuads(Vec3 P0, Vec3 T0, Vec3 N0, Vec3 P1, Vec3 T1, Vec3 N1, int n, int ne, float r, int col) {

        Vec3[] G = new Vec3[4];

        float d = distance(P0, P1) / 3;
        G[0] = P0.add(T0.mag(d)).add(N0.mult(r));
        G[1] = P0.sub(T0.mag(d)).add(N0.mult(r));
        G[2] = P1.sub(T1.mag(d)).add(N1.mult(r));
        G[3] = P1.add(T1.mag(d)).add(N1.mult(r));

        Vec3[] C = new Vec3[n];
        for (int i = 0; i < C.length; i++) {
            C[i] = origin3();
        }

        for (int i = 0; i < n; i++) {
            C[i] = bezierPoint(G, (float) (i) / (n - 1));
        }

        // displacement vectors
        Vec3[] L = new Vec3[ne];

        Vec3 T = aToB(C[0], C[1]).ab().unit();
        Vec3 LL = xyz(0, 0, 1).cross(T);
        if (LL.magSquared() < 0.01) {
            LL = xyz(1, 0, 0).cross(T);
        }
        if (LL.magSquared() < 0.01) {
            LL = xyz(0, 1, 0).cross(T);
        }
        L[0] = LL.cross(T).unit();

        Vec3[][] P = new Vec3[2][ne];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < P[i].length; j++) {
                P[i][j] = origin3();
            }
        }

        boolean dark = true;

        float[] c = new float[ne];
        float[] s = new float[ne];

        for (int j = 0; j < ne; j++) {
            c[j] = r * cos(TWO_PI * j / ne);
            s[j] = r * sin(TWO_PI * j / ne);
        }

        Vec3 I0 = L[0].unit();
        Vec3 J0 = L[0].cross(T).unit();

        for (int j = 0; j < ne; j++) {
            P[0][j] = midpoint(C[0], C[1])
                .add(I0.mult(c[j]))
                .add(J0.mult(s[j]));
        }

        int p = 1;

        for (int i = 1; i < n - 1; i++) {

            dark = !dark;

            Vec3 I = aToB(C[i - 1], C[i]).ab().unit();
            Vec3 Ip = aToB(C[i], C[i + 1]).ab().unit();
            Vec3 IpmI = Ip.sub(I);
            Vec3 N = I.cross(Ip);

            if (N.mag() < 0.001) {
                L[i] = L[i - 1];
            } else {
                L[i] = L[i - 1].add(
                    N.unit().cross(IpmI).mult(
                        m(N.unit(), I, L[i - 1])
                    )
                );
            }
            I = L[i].unit();
            Vec3 J = I.cross(Ip).unit();

            for (int j = 0; j < ne; j++) {
                P[p][j] = midpoint(C[i], C[i + 1])
                    .add(I.mult(c[j]))
                    .add(J.mult(s[j]));
            }
            p = 1 - p;

            for (int j = 0; j < ne; j++) {
                if (dark) {
                    fill(200, 200, 200);
                } else {
                    fill(col);
                }
                dark = !dark;
                int jp = (j + ne - 1) % ne;
                beginShape();
                v(P[p][jp]);
                v(P[p][j]);
                v(P[1 - p][j]);
                v(P[1 - p][jp]);
                endShape(CLOSE);
            }
        }
    }

}
