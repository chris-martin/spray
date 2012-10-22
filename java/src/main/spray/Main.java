package spray;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import thirdparty.RepeatingReleasedEventsFixer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.Timer;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static spray.Geometry.*;
import static tube.Color.*;

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[]{"spray.Main"});
    }

    GL gl;
    GLU glu;
    PGraphicsOpenGL pgogl;

    // camera target point set with mouse when pressing 't'
    List<Vec3> balls;

    Line3 view;

    Robot robot;

    boolean[] keys = new boolean[128];

    void reset() {
        view = aToB(xyz(0, -300, 100), origin3());
        balls = new ArrayList<Vec3>();
        for (float x = -300; x < 300; x+= ballRadius * 2.5) {
            for (float z = 0; z < 200; z+= ballRadius * 2.5) {
                balls.add(xyz(x, 0, z));
            }
        }
    }

    int centerX, centerY;

    int ballRadius = 6;

    final Random random = new Random();

    public void setup() {
        size(900, 500, OPENGL);
        centerX = width / 2;
        centerY = height / 2;
        sphereDetail(1);
        rectMode(CENTER);
        glu = ((PGraphicsOpenGL) g).glu;
        PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
        gl = pgl.beginGL();
        pgl.endGL();
        pgogl = (PGraphicsOpenGL) g;

        RepeatingReleasedEventsFixer.install();

        noCursor();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        reset();

        loop();

        // Ugly workaround, not sure why this is necessary
        Timer timer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loop();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void draw() {

        background(color(50, 100, 255));

        hint(ENABLE_DEPTH_TEST);
        {
            Vec3 a = view.a();
            Vec3 b = view.b();
            camera(
                a.x(), a.y(), a.z(), // eye
                b.x(), b.y(), b.z(), // center
                0, 0, -1 // up
            );
        }

        // light at a fixed point in model space
        ambientLight(100, 100, 100);
        directionalLight(250, 250, 250, -20, 10, 5);

        fill(color(100, 250, 100));
        pushMatrix();
        rect(0, 0, 5000, 5000);
        popMatrix();

        noStroke();
        fill(color(240, 240, 240));

        // render sphere of radius r and center P
        for (Vec3 ball : balls) {
            pushMatrix();
            translate(ball.x(), ball.y(), ball.z());
            sphere(ballRadius);
            popMatrix();
        }

        if (mousePressed) {
            float spread = (float) random.nextGaussian() / 5;
            float rotationAngle = random.nextFloat() * 2 * PI;
            final Line3 ray = view.b(
                rotatePointAroundLine(
                    view,
                    rotatePointAroundLine(
                        view.aOrthog(),
                        view.b(),
                        spread
                    ),
                    rotationAngle
                )
            );
            Vec3 firstCollision;
            try {
                firstCollision = Ordering.natural()
                    .onResultOf(
                        new Function<Vec3, Float>() {
                            public Float apply(Vec3 ball) {
                                return distance(ball, ray.a());
                            }
                        }
                    )
                    .min(
                        FluentIterable.from(balls)
                            .filter(new Predicate<Vec3>() {
                                public boolean apply(Vec3 ball) {
                                    return distance(ray, ball) < ballRadius * 2.5;
                                }
                            })
                    );
            } catch (NoSuchElementException e) {
                firstCollision = null;
            }
            if (firstCollision != null) {

            }
            /*balls.add(p);*/
        }

        Vec2 motion = origin2();
        Vec2 viewXY = view.ab().xy();
        if (keys['w'] && !keys['s']) { motion = motion.add(viewXY); }
        if (keys['d'] && !keys['a']) { motion = motion.add(viewXY.rot90()); }
        if (keys['s'] && !keys['w']) { motion = motion.add(viewXY.rot180()); }
        if (keys['a'] && !keys['d']) { motion = motion.add(viewXY.rot270()); }
        view = view.add(motion.mag(5).in3d());

        camera();
        hint(DISABLE_DEPTH_TEST);
        int crosshair = 10;
        stroke(black); strokeWeight(3);
        line(width / 2, height / 2 - crosshair, width/ 2 , height / 2 + crosshair);
        line(width / 2 - crosshair, height / 2, width / 2 + crosshair, height / 2);
        stroke(white); strokeWeight(1);
        line(width / 2, height / 2 - crosshair, width / 2, height / 2 + crosshair);
        line(width / 2 - crosshair, height / 2, width / 2 + crosshair, height / 2);

        if (!mousePressed && !keyPressed) {
            noLoop();
        }
    }

    Vec2 pmouse;

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e) {

        Vec2 mouse = xy(e);

        // Terrible workaround. Can't figure out how to actually determine
        // whether an event is a robot event.
        boolean isRobot = mouse.x() == 200 && mouse.y() == 200;

        if (!isRobot && pmouse != null) {

            Vec2 diff = mouse.sub(pmouse);

            float azimuth = view.ab().azimuth();
            azimuth += PI * (diff.x()) / width;

            float elevation = view.ab().elevation();
            elevation -= PI * (diff.y()) / height;
            elevation = max(-PI / 2 + 0.1f, elevation);
            elevation = min(PI / 2 - 0.1f, elevation);

            view = view.ab(view.ab().azimuth(azimuth).elevation(elevation));
            loop();
        }
        pmouse = mouse;

        if (mouse.x() < 100 || mouse.x() > 300 || mouse.y() < 100 || mouse.y() > 300) {
            robot.mouseMove(frame.getX() + 200, frame.getY() + 200);
        }
    }

    public void mousePressed() {
        loop();
    }

    public void keyPressed() {

        // reset the view
        if (key == ' ') {
            reset();
        }

        int i = (int) key;
        if (i < keys.length) {
            keys[i] = true;
        }

        loop();

    }

    public void keyReleased() {
        int i = (int) key;
        if (i < keys.length) {
            keys[i] = false;
        }
    }

    Vec3 pick() {

        pgogl.beginGL();

        int viewport[] = new int[4];
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

        double[] projection = new double[16];
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projection, 0);

        double[] modelView = new double[16];
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelView, 0);

        FloatBuffer fb = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        gl.glReadPixels(centerX, height - centerY, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fb);
        fb.rewind();

        double[] mousePosArr = new double[4];
        glu.gluUnProject(
            (double) centerX, height - (double) centerY, (double) fb.get(0),
            modelView, 0, projection, 0, viewport, 0, mousePosArr, 0);

        pgogl.endGL();

        return xyz(mousePosArr);
    }

}
