package spray;

import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import spray.Geometry.Vec3;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static spray.Geometry.origin3;
import static spray.Geometry.xyz;
import static tube.Color.*;

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[]{"spray.Main"});
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

        // to reset the z-buffer used above for picking
        // background(white);

        // shows local frame aligned with screen when picked ('q' pressed) using (R,G,B) lines
        noStroke();
        show(Q, 30, I, J, K);

        // shows picked point in cyan (it is set when 't' is pressed and becomes the focus)
        noStroke();
        fill(cyan);
        show(T, 2);

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

    }

    // ************************ Graphic pick utilities *******************************

    // camera target point set with mouse when pressing 't'
    Vec3 T = origin3();

    // eye and lookAt
    Vec3 E = origin3(), L = origin3();

    // view parameters: distance, angles, q
    float d = 300, b = 0, a = 0;

    Boolean showModel = true;

    // track if the frame has already been set
    Boolean[] first = {true, true, true};

    // picked surface point Q and screen aligned vectors {I,J,K} set when picked
    Vec3 Q = origin3();
    Vec3 I = origin3(), J = origin3(), K = origin3();

    void changeViewAndFrame() {

        // viewing direction angles
        float ca = cos(a), sa = sin(a), cb = cos(b), sb = sin(b);

        // changes distance form the target to the viewpoint
        if (keyPressed && key == 'd') {
            d -= mouseY - pmouseY;
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

}
