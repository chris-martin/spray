package spray;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.Timer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Ranges;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import thirdparty.RepeatingReleasedEventsFixer;

import static com.google.common.collect.Lists.newArrayList;
import static spray.Geometry.*;
import static tube.Color.black;
import static tube.Color.white;

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[]{"spray.Main"});
    }

    GL gl;
    GLU glu;
    PGraphicsOpenGL pgogl;

    // camera target point set with mouse when pressing 't'
    Collection<Vec3> balls;

    Line3 view;

    Robot robot;

    boolean[] keys = new boolean[128];

    void reset() {
        view = pointAndStep(xyz(0, -300, 100), xyz(0, 300, 0));
        balls = new HashSet<Vec3>();
        for (float x = -300; x < 300; x+= ballRadius * 2 + 1) {
            for (float z = 0; z < 200; z+= ballRadius * 2 + 1) {
                balls.add(xyz(x, 0, z));
            }
        }
    }

    int ballRadius = 8;

    final Random random = new Random();

    public void setup() {
        size(900, 500, OPENGL);
        frame.setLocation(0, 0);
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
            robot = new Robot(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1]);
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
            for (int i = 0; i < 5; i++) {
                if (mouseButton == LEFT) {
                    Vec3 ball = fire();
                    if (ball != null) {
                        balls.add(ball);
                    }
                } else {
                    Vec3 ball = unfire();
                    if (ball != null) {
                        balls.remove(ball);
                    }
                }
            }
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

    Line3 ray(float spread) {
        spread *= (float) random.nextGaussian();
        float rotationAngle = random.nextFloat() * 2 * PI;
        return view.b(
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
    }

    Vec3 rayHitBall(final Line3 ray) {
        try {
            return Ordering.natural()
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
                                return distance(ray, ball) < ballRadius + 1;
                            }
                        })
                );
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    Vec3 unfire() {
        Line3 ray = ray(0.07f);
        return rayHitBall(ray);
    }

    Vec3 fire() {

        final Line3 ray = ray(0.1f);

        final Vec3 c1 = rayHitBall(ray);
        if (c1 == null) {
            return null;
        }

        final List<Vec3> closeBalls = newArrayList(FluentIterable.from(balls)
            .filter(new Predicate<Vec3>() {
                public boolean apply(Vec3 ball) {
                    float d = distance(c1, ball);
                    return d > 0.1 && d < ballRadius * 4;
                }
            }));

        final Vec3 ball1 = c1.add(view.ab().mag(-1 * ballRadius));

        final Line3 axis1 = pointAndStep(c1, view.ab().orthog());

        Vec3[] ballAndC2 = FluentIterable
            .from(Ranges.closedOpen(0, 1000).asSet(DiscreteDomains.integers()))
            .transform(new Function<Integer, Vec3>() {
                public Vec3 apply(Integer i) {
                    float angle = (float) (i / 1000.) * 2 * PI;
                    return rotatePointAroundLine(axis1, ball1, angle);
                }
            })
            .transformAndConcat(new Function<Vec3, Iterable<Vec3[]>>() {
                public Iterable<Vec3[]> apply(final Vec3 ball2) {
                    return FluentIterable
                        .from(closeBalls)
                        .filter(new Predicate<Vec3>() {
                            public boolean apply(Vec3 closeBall) {
                                return distance(ball2, closeBall) < ballRadius + 1;
                            }
                        })
                        .transform(new Function<Vec3, Vec3[]>() {
                            public Vec3[] apply(Vec3 closeBall) {
                                return new Vec3[]{ball2, closeBall};
                            }
                        });
                }
            })
            .first()
            .orNull();

        final Vec3 ball2;
        Vec3 c2;
        if (ballAndC2 != null) {
            ball2 = ballAndC2[0];
            c2 = ballAndC2[1];
        } else {
            ball2 = ball1;
            c2 = null;
        }

        Vec3 ball3;
        if (c2 == null) {
            ball3 = ball2;
        } else {
            closeBalls.remove(c2);
            final Line3 axis2 = aToB(c1, c2);
            return FluentIterable
                .from(Ranges.closedOpen(0, 1000).asSet(DiscreteDomains.integers()))
                .transform(new Function<Integer, Vec3>() {
                    public Vec3 apply(Integer i) {
                        float angle = (float) (i / 1000.) * 2 * PI;
                        return rotatePointAroundLine(axis2, ball2, angle);
                    }
                })
                .filter(new Predicate<Vec3>() {
                    public boolean apply(final Vec3 ball3) {
                        return FluentIterable
                            .from(closeBalls)
                            .anyMatch(new Predicate<Vec3>() {
                                public boolean apply(Vec3 closeBall) {
                                    return distance(ball3, closeBall) < ballRadius + 2;
                                }
                            });
                    }
                })
                .first()
                .or(ball2);
        }

        return ball3;
    }

    Vec2 pmouse;

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    boolean robotMouseEvent;

    public void mouseMoved(MouseEvent e) {

        Vec2 mouse = xy(e);

        if (robotMouseEvent) {
            robotMouseEvent = false;
        } else if (pmouse != null) {

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
            robotMouseEvent = true;
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

        int centerX = width / 2, centerY = height / 2;

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
