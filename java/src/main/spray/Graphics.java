package spray;

import spray.Geometry.Line;
import spray.Geometry.Vec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static spray.Geometry.aToB;
import static spray.Geometry.xy;

public class Graphics {

  public static void main(String[] args) {
    new Graphics();
  }

  Dimension screenSize = new Dimension(800, 600);
  JFrame frame;
  JPanel panel;
  Mousing mousing = new Mousing();
  int fps = 30;

  public Graphics() {
    frame = new JFrame("Spray");
    frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    frame.addKeyListener(new Keying());
    FlowLayout layout = new FlowLayout(); layout.setVgap(0); layout.setHgap(0);
    panel = new JPanel(layout);
    panel.addMouseListener(mousing); panel.addMouseMotionListener(mousing);
    frame.add(panel);
    restart();

    new Timer(1000/fps, new ActionListener() { public void actionPerformed(ActionEvent e) {
      frame.repaint();
    }}).start();

  }

  void quit() {
    WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
  }

  void restart() {
    rebuildPainters();
  }

  void rebuildPainters() {

    final PainterComponent comp = new PainterComponent();
    comp.setPreferredSize(screenSize);

    panel.removeAll();
    panel.add(comp);
    frame.pack();
    frame.setVisible(true);
    frame.setResizable(false);
  }

  class Mousing extends MouseAdapter {
    Vec a;
    Line motion(MouseEvent e) {
      Vec b = xy(e);
      Line motion = a == null ? null : aToB(a, b);
      a = b;
      return motion;
    }

    public void mouseMoved(MouseEvent e) {
      Line motion = motion(e);
    }

    public void mouseDragged(MouseEvent event) {
      Line motion = motion(event);
    }

    public void mouseExited(MouseEvent event) {
      a = null;
    }

    public void mousePressed(MouseEvent event) {
      Vec p = xy(event);
    }
    public void mouseReleased(MouseEvent e) {
      a = null;
    }
    void select(final Vec p) {
    }
  }

  class Keying extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      char C = e.getKeyChar();
      char c = Character.toLowerCase(C);
      boolean upper = C != c;
      switch (c) {
      }
    }
  }

  static class PainterComponent extends JPanel {
    private final Painter painter; PainterComponent(Painter painter) { this.painter = painter; }
    PainterComponent(Painter... ps) { this(painterList(ps)); }
    public void paint(java.awt.Graphics g_) { super.paintComponent(g_);
      Graphics2D g = ((Graphics2D) g_);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      painter.paint(g); } }

  interface Painter { void paint(Graphics2D g); }

  static <P extends Painter> PainterList<P> painterList() { return new PainterList<P>(); }
  static <P extends Painter> PainterList<P> painterList(P... ps) { return new PainterList<P>(ps); }
  static class PainterList<P extends Painter> implements Painter {
    private final List<P> painters = newArrayList();
    public PainterList() {}
    public PainterList(P... ps) { for (P p : ps) add(p); }
    public void paint(Graphics2D g) { for (P p : painters) p.paint(g); }
    public void add(P p) { painters.add(p); }
  }

}
