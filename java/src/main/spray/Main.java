package spray;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.Timer;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import spray.Mesh.Triangle;
import spray.Mesh.Vertex;
import thirdparty.RepeatingReleasedEventsFixer;

import static com.google.common.collect.Lists.newArrayList;
import static spray.Geometry.*;
import static tube.Color.black;
import static tube.Color.white;

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[]{"spray.Main"});
    }

    final Object meshLock = new Object();

    GL gl;
    GLU glu;
    PGraphicsOpenGL pgogl;
    Balls<Vec3> balls;
    Line3 view;
    Robot robot;
    Vec2 pmouse;
    boolean robotMouseEvent;
    final Random random = new Random();
    List<Triangle> triangles;
    boolean showBalls;

    boolean[] keys = new boolean[128];

    void resetView() {
        view = pointAndStep(xyz(0, -300, 100), xyz(0, 300, 0));
    }

    void cone() {
        resetView();
        synchronized (meshLock) {
            triangles = newArrayList();
            balls = new Balls<Vec3>();

            float angle = 0;
            float height = 0;
            float mag = 100;
            for (int i = 0; i < 1000; i++) {
                height += 0.5;
                angle += 0.5;
                mag -= 0.1;
                balls.balls.add(angleVec2(angle, mag).in3d().addZ(height).add(
                    xyz(random.nextFloat(), random.nextFloat(), random.nextFloat())
                ));
            }

            loop();
        }
    }

    void wall() {
        resetView();
        synchronized (meshLock) {
            triangles = newArrayList();
            balls = new Balls<Vec3>();

            for (int y = 0; y < 2; y++) {
                for (float x = -200; x < 200; x+= balls.radius * 2) {
                    for (float z = 0; z < 200; z+= balls.radius * 2) {
                        balls.balls.add(xyz(
                            x + random.nextFloat(),
                            y * balls.radius * 2 + random.nextFloat() * 4,
                            z + random.nextFloat()));
                    }
                }
            }

            loop();
        }
    }

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

        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                mouseWheel(evt.getWheelRotation());
            }
        });

        resetView();
        triangles = newArrayList();
        balls = new Balls<Vec3>();

        loop();

        // Ugly workaround, not sure why this is necessary
        Timer timer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loop();
            }
        });
        timer.setRepeats(false);
        timer.start();

        Thread t = new Thread("mesh") {
            public void run() {

                ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "mesh-executor");
                    }
                });

                Stopwatch stopwatch = new Stopwatch();

                while (true) {
                    try {
                        stopwatch.reset();
                        stopwatch.start();
                        final Balls balls;
                        synchronized (meshLock) {
                            balls = new Balls<Vec3>(Main.this.balls.balls);
                        }
                        Future<Mesh> meshFuture = executor.submit(new Callable<Mesh>() {
                            public Mesh call() throws Exception {
                                return new Mesh(balls);
                            }
                        });
                        Mesh mesh = null;
                        try {
                            mesh = meshFuture.get(3, TimeUnit.SECONDS);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            System.err.println("Mesh calculation timed out");
                        }
                        meshFuture.cancel(true);
                        if (mesh != null) {
                            synchronized (meshLock) {
                                triangles = mesh.triangles();
                                loop();
                            }
                        }
                        stopwatch.stop();
                        System.out.println(stopwatch.toString());
                        Thread.yield();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();

        wall();
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

        ambientLight(100, 100, 100);
        directionalLight(250, 250, 250, -20, 10, 5);
        directionalLight(250, 250, 250, 20, -10, 5);

        fill(color(100, 250, 100));
        pushMatrix();
        rect(0, 0, 5000, 5000);
        popMatrix();

        fill(color(240, 240, 240));
        noStroke();

        synchronized (meshLock) {

            if (mousePressed) {
                int i = (int) Math.max(1, (spread * spread * 20));
                while (i-- > 0) {
                    if (mouseButton == LEFT) {
                        Vec3 ball = balls.rayPack(ray());
                        if (ball != null) {
                            balls.balls.add(ball);
                        }
                    } else {
                        Vec3 ball = balls.raySearch(ray());
                        if (ball != null) {
                            balls.balls.remove(ball);
                        }
                    }
                }
            }

            for (Triangle t : triangles) {

                Iterator<Vertex> vertices = t.vertices().iterator();
                Vec3 a = vertices.next().asVec3();
                Vec3 b = vertices.next().asVec3();
                Vec3 c = vertices.next().asVec3();

                pushMatrix();
                beginShape(TRIANGLES);
                vertex(a.x(), a.y(), a.z());
                vertex(b.x(), b.y(), b.z());
                vertex(c.x(), c.y(), c.z());
                endShape();
                popMatrix();
            }

            if (showBalls) {

                strokeWeight(1);
                stroke(0, 0, 0);
                fill(color(255, 0, 0));

                for (Vec3 ball : FluentIterable.from(balls.balls)) {
                    pushMatrix();
                    translate(ball.x(), ball.y(), ball.z());
                    sphere(2);
                    popMatrix();
                }
            }

        }

        strokeWeight(1);
        stroke(color(255, 0, 0));
        Line3 tiltedView = aToB(
            view.mult(0.25f).b(),
            rotatePointAroundLine(view.aOrthog(), view.mult(2).b(), spread)
        ).mult(60);
        float rotationAngle = 2 * PI / 7;
        for (int i = 0; i < 7; i++) {
            pushMatrix();
            beginShape(LINE);
            if (i != 0) {
                tiltedView = aToB(
                    rotatePointAroundLine(view, tiltedView.a(), rotationAngle),
                    rotatePointAroundLine(view, tiltedView.b(), rotationAngle)
                );
            }
            vertex(tiltedView.a().x(), tiltedView.a().y(), tiltedView.a().z());
            vertex(tiltedView.b().x(), tiltedView.b().y(), tiltedView.b().z());
            endShape();
            popMatrix();
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

    float spread = 0.1f;

    Line3 ray() {
        float spread;
        do {
            spread = distance(origin2(), xy(random.nextFloat()*2-1, random.nextFloat()*2-1));
        } while (spread > 1);
        spread *= this.spread;
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

    public void mouseWheel(int delta) {
        spread += 0.025 * delta;
        spread = Math.max(spread, 0.025f);
        spread = Math.min(spread, 1f);
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

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
            elevation = max(PI / -2 + 0.5f, elevation);
            elevation = min(PI / 2 - 0.5f, elevation);

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

        switch (key) {
            case '1':
                wall();
                break;
            case '2':
                cone();
                break;
            case 'b':
                showBalls = !showBalls;
                break;
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
