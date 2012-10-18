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

    // camera target point set with mouse when pressing 't'
    Vec3 target = origin3();

    Vec3 eye = origin3();

    Vec3 lookAt = origin3();

    // view distance
    float d = 300;

    // angles
    float a, b;

    // picked surface point Q and screen aligned vectors {I,J,K} set when picked
    Vec3 Q = origin3();

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
        // Q and target and for picking the local frames 1 and 2
        fill(yellow);
        pushMatrix();
        rotateY(PI / 2);
        rect(0, 0, 400, 400);
        popMatrix();

        // sets the target point target where the mouse points.
        // The camera will turn toward's it when the 't' key is released
        if (keyPressed && key == 't') {
            target = Pick();
        }

        // sets Q to the picked surface-point and {I,J,K} to screen aligned vectors
        if (keyPressed && key == 'q') {
            SetFrameFromPick();
        }

        // to reset the z-buffer used above for picking
        // background(white);

        // shows local frame aligned with screen when picked ('q' pressed) using (R,G,B) lines
        noStroke();
        show(Q, 5);

        // shows picked point in cyan (it is set when 't' is pressed and becomes the focus)
        noStroke();
        fill(cyan);
        show(target, 2);

    }

    // camera rotation around target when no key is pressed
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
            lookAt = target;
        }
    }

    public void keyPressed() {

        // reset the view
        if (key == ' ') {
            d = 300;
            b = 0;
            a = 0;
            lookAt = origin3();
        }

    }

    void changeViewAndFrame() {

        // viewing direction angles
        float ca = cos(a), sa = sin(a), cb = cos(b), sb = sin(b);

        // changes distance form the target to the viewpoint
        if (keyPressed && key == 'd') {
            d -= mouseY - pmouseY;
        }

        // sets the eye
        eye = xyz(d * cb * ca, d * sa, d * sb * ca);

        // defines the view : eye, ctr, up
        camera(eye.x(), eye.y(), eye.z(), lookAt.x(), lookAt.y(), lookAt.z(), 0.0f, 1.0f, 0.0f);

        // puts a white light above and to the left of the viewer
        directionalLight(250, 250, 250, -eye.x(), -eye.y() + 100, -eye.z());

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

    // render sphere of radius r and center P
    void show(Vec3 P, float r) {
        pushMatrix();
        translate(P.x(), P.y(), P.z());
        sphere(r);
        popMatrix();
    }

}
