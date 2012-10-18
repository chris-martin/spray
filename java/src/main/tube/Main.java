package tube;

import processing.core.PApplet;

import processing.opengl.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import java.nio.*;

import static tube.pt.*;
import static tube.vec.*;

//*********************************************************************
//**      3D viewer with camera control and surface picking          **
//**              Jarek Rossignac, October 2010                      **
//**                    (using PVectors)                             **
//*********************************************************************

public class Main extends PApplet {

  public static void main(String[] args) {
    PApplet.main(new String[]{ "tube.Main" });
  }

  GL gl;
  GLU glu;

  public void setup() {
    size(900, 500, OPENGL);
    setColors();
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
    rotateY(PI/2);
    rect(0,0,400,400);
    popMatrix();

    // sets the target point T where the mouse points.
    // The camera will turn toward's it when the 't' key is released
    if (keyPressed && key=='t') {
      T.set(Pick());
    }

    // sets Q to the picked surface-point and {I,J,K} to screen aligned vectors
    if (keyPressed && key=='q') {
      SetFrameFromPick(Q,I,J,K);
    }

    // the following 2 actions set frame (1 or 2) and edit its origin as the mouse is dragged
    if (keyPressed && key=='1') {
      m=1;
      SetFrameFromPick(Q,I,J,K);
      mQ[m].set(Q);
      if(first[m]) {
        mI[m].set(I);
        mJ[m].set(J);
        mK[m].set(K);
        first[m]=false;
      }
    }
    if (keyPressed && key=='2') {
      m=2;
      SetFrameFromPick(Q,I,J,K);
      mQ[m].set(Q);
      if(first[m]) {
        mI[m].set(I);
        mJ[m].set(J);
        mK[m].set(K);
        first[m]=false;
      }
    }

    // to reset the z-buffer used above for picking
    // background(white);

    // shows local frame aligned with screen when picked ('q' pressed) using (R,G,B) lines
    noStroke();
    show(Q,30,I,J,K);

    // shows picked point in cyan (it is set when 't' is pressed and becomes the focus)
    noStroke();
    fill(cyan);
    show(T,2);

    // shows origin of frame 1 and 2 as a small red or green ball
    fill(red);
    show(mQ[1],3);
    fill(green);
    show(mQ[2],3);

    if(m==1) {
      fill(red);
    } else if(m==2) {
      fill(green);
    } else if(m==0) {
      fill(blue);
    }

    // shows origin of selected frame (R,G,B) for (1,2,0) as a bigger ball
    show(mQ[m],5);

    // shows current point on surface. Changed when 'q' is pressed
    fill(yellow);
    show(Q,2);

    // shows second model (currently axes)
    noStroke();
    showModel();

    // showTube(mQ[1],mK[1],mQ[2],mK[2],10);
    showQuads(mQ[1], mK[1], mJ[1], mQ[2], mK[2], mJ[2], 20, 20, 20, green);

  }

  // ****************** INTERRUPTS *************************

  // camera rotation around T when no key is pressed
  public void mouseDragged() {
    if(keyPressed) {
      return;
    }
    a-=PI*(mouseY-pmouseY)/height;
    a=max(-PI/2+0.1f,a);
    a=min(PI/2-0.1f,a);
    b+=PI*(mouseX-pmouseX)/width;
  }

  // sets the new focus point to wher ethe mous points to when the mouse-button is released
  public void keyReleased() {
    if(key=='t') {
      L.x=T.x; L.y=T.y; L.z=T.z;
    }
  }

  public void keyPressed() {

    // toggles shaded versus wireframe viewing of the first model
    if (key=='m') showModel=!showModel;

    // reset the view
    if (key==' ') {d=300; b=0; a=0; L.y=0; L.x=0; L.z=0;}

    // reset the current frame to be defined by the mouse position and the screen orientation
    if (key=='h') {mQ[m].set(Q); mI[m].set(I); mJ[m].set(J); mK[m].set(K); }
  }

  // ************************ Graphic pick utilities *******************************

  // camera target point set with mouse when pressing 't'
  pt T = P();

  // eye and lookAt
  pt E = P(), L=P();

  pt[] mQ= new pt [3];

  // three local frames {Q,I,J,K}
  vec[] mI= new vec [3], mJ= new vec [3], mK= new vec [3];

  // which frame is being shown / edited
  int m=1;

  // view parameters: distance, angles, q
  float d=300, b=0, a=0;

  Boolean showModel=true;

  // track if the frame has already been set
  Boolean [] first = {true, true, true};

  // picked surface point Q and screen aligned vectors {I,J,K} set when picked
  pt Q=P(); vec I=V(), J=V(), K=V();

  // declares the local frames
  void initm() {for(int i=0; i<3; i++) {mQ[i]=P(); mI[i]=V(); mJ[i]=V(); mK[i]=V(); }}

  void  changeViewAndFrame() {

    // viewing direction angles
    float ca=cos(a), sa=sin(a), cb=cos(b), sb=sin(b);

    // moves the selected frame parallel to the screen
    if (keyPressed&&key=='x') {
      mQ[m].add(mouseX-pmouseX,I).add(pmouseY-mouseY,J);
    }

    // moves the selected frame on th ehorizontal plane
    if (keyPressed&&key=='z') {
      mQ[m].add(mouseX-pmouseX,I).add(mouseY-pmouseY,K);
    }

    // changes distance form the target to the viewpoint
    if (keyPressed&&key=='d') {
      d-=mouseY-pmouseY;
    }

    // rotates current frame parallel to the screen
    if (keyPressed && key == 'a') {
      float a = (float) (-mouseY+pmouseY+mouseX-pmouseX)/width; mI[m].rotate(a,I,J);
      mJ[m].rotate(a,I,J);
      mK[m].rotate(a,I,J);
    }

    // rotates the current frames in pitch and yaw
    if (keyPressed&&key=='r') {

      float a = (float) (mouseY-pmouseY)/width;
      mI[m].rotate(a,J,K);
      mJ[m].rotate(a,J,K);
      mK[m].rotate(a,J,K);

      float b = (float) (pmouseX-mouseX)/width;
      mI[m].rotate(b,I,K);
      mJ[m].rotate(b,I,K);
      mK[m].rotate(b,I,K);
    }

    // sets the eye
    E.set(d*cb*ca, d*sa, d*sb*ca);

    // defines the view : eye, ctr, up
    camera(E.x, E.y, E.z, L.x, L.y, L.z, 0.0f, 1.0f, 0.0f);

    // puts a white light above and to the left of the viewer
    directionalLight(250, 250, 250, -E.x, -E.y+100, -E.z);

    // in case you want the light to be fixed in model space
    // ambientLight(100,100,0);
    // directionalLight(250, 250, 0, -20, 10, 5);
    // directionalLight(100, 100, 250, 10, -20, -5);
  }

  // sets Q where the mouse points to and I, J, K to be aligned with the screen (I right, J up, K towards thre viewer)
  void SetFrameFromPick(pt Q, vec I, vec J, vec K) {
    glu = ((PGraphicsOpenGL) g).glu;
    PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
    float modelviewm[] = new float[16];
    gl = pgl.beginGL();
    gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, modelviewm, 0);
    pgl.endGL();
    Q.set(Pick());
    I.set(modelviewm[0],modelviewm[4],modelviewm[8]);
    J.set(modelviewm[1],modelviewm[5],modelviewm[9]);
    K.set(modelviewm[2],modelviewm[6],modelviewm[10]);
    // println(I.x+","+I.y+","+I.z);
  }

  pt Pick() {
    ((PGraphicsOpenGL)g).beginGL();
    int viewport[] = new int[4];
    double[] proj=new double[16];
    double[] model=new double[16];
    gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
    gl.glGetDoublev(GL.GL_PROJECTION_MATRIX,proj,0);
    gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX,model,0);
    FloatBuffer fb=ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    gl.glReadPixels(mouseX, height-mouseY, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fb);
    fb.rewind();
    double[] mousePosArr=new double[4];
    glu.gluUnProject(
      (double) mouseX, height - (double) mouseY, (double) fb.get(0),
      model, 0, proj, 0, viewport, 0, mousePosArr, 0 );
    ((PGraphicsOpenGL)g).endGL();
    return P((float)mousePosArr[0],(float)mousePosArr[1],(float)mousePosArr[2]);
  }

  // ********************** display utilities ****************************

  // sets the matrix and displays the second model (here the axes as blocks)
  void showModel() {
    pushMatrix();
    applyMatrix(
        mI[m].x, mJ[m].x, mK[m].x, mQ[m].x,
        mI[m].y, mJ[m].y, mK[m].y, mQ[m].y,
        mI[m].z, mJ[m].z, mK[m].z, mQ[m].z,
        0.0f,    0.0f,    0.0f,    1.0f      );

    // replace this (showing the axes) with code for showing your second model
    showAxes(30);
    popMatrix();
  }

  // shows three orthogonal axes as red, green, blue blocks aligned with the local frame coordinates
  void showAxes(float s) {
    noStroke();
    pushMatrix();
    pushMatrix(); fill(red);   scale(s,1,1); box(2); popMatrix();
    pushMatrix(); fill(green); scale(1,s,1); box(2); popMatrix();
    pushMatrix(); fill(blue);  scale(1,1,s); box(2); popMatrix();
    popMatrix();
  }

  int red, yellow, green, cyan, blue, magenta, dred, dyellow, dgreen, dcyan, dblue,
      dmagenta, white, black, orange, grey, metal, dorange, brown, dbrown;
  void setColors() {
    red = color(250,0,0);        dred = color(150,0,0);
    magenta = color(250,0,250);  dmagenta = color(150,0,150);
    blue = color(0,0,250);     dblue = color(0,0,150);
    cyan = color(0,250,250);     dcyan = color(0,150,150);
    green = color(0,250,0);    dgreen = color(0,150,0);
    yellow = color(250,250,0);    dyellow = color(150,150,0);
    orange = color(250,150,0);    dorange = color(150,50,0);
    brown = color(150,150,0);     dbrown = color(50,50,0);
    white = color(250,250,250); black = color(0,0,0); grey = color(100,100,100); metal = color(150,150,250);
  }

  //*********************************************************************
  //**                      3D geeomtry tools                          **
  //**              Jarek Rossignac, October 2010                      **
  //**                                                                 **
  //*********************************************************************

  // mouse

  // current mouse location
  pt Mouse() {return P(mouseX,mouseY,0);}

  pt Pmouse() {return P(pmouseX,pmouseY,0);}

  // vector representing recent mouse displacement
  vec MouseDrag() {return V(mouseX-pmouseX,mouseY-pmouseY,0);}

  // measures

  //U*V dot product
  static float d(vec U, vec V) {return U.x*V.x+U.y*V.y+U.z*V.z; }

  // (UxV)*W  mixed product, determinant
  static float m(vec U, vec V, vec W) {return d(U,N(V,W)); }

  // det (EA EB EC) is >0 when E sees (A,B,C) clockwise
  static float m(pt E, pt A, pt B, pt C) {return m(V(E,A),V(E,B),V(E,C));}

  // V*V    norm squared
  static float n2(vec V) {return sq(V.x)+sq(V.y)+sq(V.z);}

  // ||V||  norm
  static float n(vec V) {return sqrt(n2(V));}

  // ||AB|| distance
  static float d(pt P, pt Q) {return sqrt(sq(Q.x-P.x)+sq(Q.y-P.y)+sq(Q.z-P.z)); }

  // area of triangle
  static float area(pt A, pt B, pt C) {return n(N(A,B,C))/2; }

  // volume of tet
  static float volume(pt A, pt B, pt C, pt D) {return m(V(A,B),V(A,C),V(A,D))/6; }

  // true if U and V are almost parallel
  static boolean parallel (vec U, vec V) {return n(N(U,V))<n(U)*n(V)*0.00001; }

  // angle(U,V)
  static float angle(vec U, vec V) {return acos(d(U,V)/n(V)/n(U)); }

  // (UxV)*W>0  U,V,W are clockwise
  static boolean cw(vec U, vec V, vec W) {return m(U,V,W)>0; }

  // tet is oriented so that A sees B, C, D clockwise
  static boolean cw(pt A, pt B, pt C, pt D) {return volume(A,B,C,D)>0; }

  // rotate

  // rotated 90 degrees in XY plane
  static vec R(vec V) {return V(-V.y,V.x,V.z);}

  // Rotated P by a around G in plane (I,J)
  static pt R(pt P, float a, vec I, vec J, pt G) {
    float x=d(V(G,P),I), y=d(V(G,P),J);
    float c=cos(a), s=sin(a);
    return P(P,x*c-x-y*s,I,x*s+y*c-y,J);
  }

  // Rotated V by a parallel to plane (I,J)
  static vec R(vec V, float a, vec I, vec J) {
    float x=d(V,I), y=d(V,J);
    float c=cos(a), s=sin(a);
    return A(V,V(x*c-x-y*s,I,x*s+y*c-y,J));
  }

  // render

  // changes normal for smooth shading
  void normal(vec V) {normal(V.x,V.y,V.z);}

  // vertex for shading or drawing
  void v(pt P) {vertex(P.x,P.y,P.z);}

  // vertex with texture coordinates
  void vTextured(pt P, float u, float v) {vertex(P.x,P.y,P.z,u,v);}

  // draws edge (P,Q)
  void show(pt P, pt Q) {line(Q.x,Q.y,Q.z,P.x,P.y,P.z); }

  // shows edge from P to P+V
  void show(pt P, vec V) {line(P.x,P.y,P.z,P.x+V.x,P.y+V.y,P.z+V.z); }

  // shows edge from P to P+dV
  void show(pt P, float d, vec V) {line(P.x,P.y,P.z,P.x+d*V.x,P.y+d*V.y,P.z+d*V.z); }

  // volume of tet
  void show(pt A, pt B, pt C) {beginShape(); v(A);v(B); v(C); endShape(CLOSE);}

  // volume of tet
  void show(pt A, pt B, pt C, pt D) {beginShape(); v(A); v(B); v(C); v(D); endShape(CLOSE);}

  // render sphere of radius r and center P
  void show(pt P, float r) {pushMatrix(); translate(P.x,P.y,P.z); sphere(r); popMatrix();}

  // render sphere of radius r and center P
  void show(pt P, float s, vec I, vec J, vec K) {
    noStroke();
    fill(yellow); show(P,5);
    stroke(red); show(P,s,I);
    stroke(green); show(P,s,J);
    stroke(blue); show(P,s,K);
  }

  // prints string s in 3D at P
  void show(pt P, String s) {text(s, P.x, P.y, P.z); }

  // prints string s in 3D at P+D
  void show(pt P, String s, vec D) {text(s, P.x+D.x, P.y+D.y, P.z+D.z);  }

  // curve

  // draws a cubic Bezier curve with control points A, B, C, D
  void bezier(pt A, pt B, pt C, pt D) {
    bezier(A.x,A.y,A.z,B.x,B.y,B.z,C.x,C.y,C.z,D.x,D.y,D.z);
  }

  // draws a cubic Bezier curve with control points A, B, C, D
  void bezier(pt [] C) {bezier(C[0],C[1],C[2],C[3]);}

  pt bezierPoint(pt[] C, float t) {
    return P(
      bezierPoint(C[0].x,C[1].x,C[2].x,C[3].x,t),
      bezierPoint(C[0].y,C[1].y,C[2].y,C[3].y,t),
      bezierPoint(C[0].z,C[1].z,C[2].z,C[3].z,t)
    );
  }

  vec bezierTangent(pt[] C, float t) {
    return V(
      bezierTangent(C[0].x,C[1].x,C[2].x,C[3].x,t),
      bezierTangent(C[0].y,C[1].y,C[2].y,C[3].y,t),
      bezierTangent(C[0].z,C[1].z,C[2].z,C[3].z,t)
    );
  }

  // draws cubic Bezier interpolating (P0,T0) and (P1,T1)
  void PT(pt P0, vec T0, pt P1, vec T1) {
    float d=d(P0,P1)/3;
    bezier(P0, P(P0,-d,U(T0)), P(P1,-d,U(T1)), P1);
  }

  // draws cubic Bezier interpolating (P0,T0) and (P1,T1)
  void PTtoBezier(pt P0, vec T0, pt P1, vec T1, pt [] C) {
    float d=d(P0,P1)/3;
    C[0].set(P0);
    C[1].set(P(P0,-d,U(T0)));
    C[2].set(P(P1,-d,U(T1)));
    C[3].set(P1);
  }

  vec vecToCubic (pt A, pt B, pt C, pt D, pt E) {
    return V(
      (-A.x+4*B.x-6*C.x+4*D.x-E.x)/6,
      (-A.y+4*B.y-6*C.y+4*D.y-E.y)/6,
      (-A.z+4*B.z-6*C.z+4*D.z-E.z)/6
    );
  }

  vec vecToProp (pt B, pt C, pt D) {
    float cb=d(C,B), cd=d(C,D);
    return V(C,P(B,cb/(cb+cd),D));
  }

  // perspective

  pt Pers(pt P, float d) {
    return P(d*P.x/(d+P.z), d*P.y/(d+P.z), d*P.z/(d+P.z) );
  }

  pt InverserPers(pt P, float d) {
    return P(d*P.x/(d-P.z), d*P.y/(d-P.z), d*P.z/(d-P.z) );
  }

  // intersection

  // if (P,Q) intersects (A,B,C), return true and set X to the intersection point
  boolean intersect(pt P, pt Q, pt A, pt B, pt C, pt X)  {
    return intersect(P,V(P,Q),A,B,C,X);
  }

  // if ray from E along T intersects triangle (A,B,C), return true and set X to the intersection point
  boolean intersect(pt E, vec T, pt A, pt B, pt C, pt X) {
    vec EA=V(E,A), EB=V(E,B), EC=V(E,C), AB=V(A,B), AC=V(A,C);
    boolean s=cw(EA,EB,EC), sA=cw(T,EB,EC), sB=cw(EA,T,EC), sC=cw(EA,EB,T);
    if ( (s==sA) && (s==sB) && (s==sC) ) {
      return false;
    }
    float t = m(EA,AC,AB) / m(T,AC,AB);
    X.set(P(E,t,T));
    return true;
  }

  // true if ray from E with direction T hits triangle (A,B,C)
  boolean rayIntersectsTriangle(pt E, vec T, pt A, pt B, pt C) {
    vec EA=V(E,A), EB=V(E,B), EC=V(E,C);
    boolean s=cw(EA,EB,EC), sA=cw(T,EB,EC), sB=cw(EA,T,EC), sC=cw(EA,EB,T);
    return  (s==sA) && (s==sB) && (s==sC);
  }

  boolean edgeIntersectsTriangle(pt P, pt Q, pt A, pt B, pt C)  {
    vec PA=V(P,A), PQ=V(P,Q), PB=V(P,B), PC=V(P,C), QA=V(Q,A), QB=V(Q,B), QC=V(Q,C);
    boolean p=cw(PA,PB,PC), q=cw(QA,QB,QC), a=cw(PQ,PB,PC), b=cw(PA,PQ,PC), c=cw(PQ,PB,PQ);
    return (p!=q) && (p==a) && (p==b) && (p==c);
  }

  float rayParameterToIntersection(pt E, vec T, pt A, pt B, pt C) {
    vec AE=V(A,E), AB=V(A,B), AC=V(A,C);
    return - m(AE,AC,AB) / m(T,AC,AB);
  }

  // returns angle in 2D dragged by the mouse around the screen projection of G
  float angleDraggedAround(pt G) {
    pt S=P(screenX(G.x,G.y,G.z),screenY(G.x,G.y,G.z),0);
    vec T=V(S,Pmouse()); vec U=V(S,Mouse());
    return atan2(d(R(U),T),d(U,T));
  }

  float scaleDraggedFrom(pt G) {
    pt S=P(screenX(G.x,G.y,G.z),screenY(G.x,G.y,G.z),0);
    return d(S,Mouse())/d(S,Pmouse());
  }

  // TUBE
  void showTube(pt P0, vec T0, pt P1, vec T1, int n) {

    pt[] C = new pt[4];
    makePts(C);

    // shows an interpolating Bezier curve from frame 1 to frames 2
    // (tangents are defined by K vectors)
    stroke(cyan);
    noFill();
    PTtoBezier(P0,T0,P1,T1,C);
    bezier(C);
    noStroke();

    for (float t=0; t<=1; t+=0.1) {
      pt B=bezierPoint(C,t);
      vec T=bezierTangent(C,t);
      stroke(magenta);
      show(B,0.1f,T);
      noStroke();
      fill(brown);
      show(B,1);
    }
  }

  void showQuads(pt P0, vec T0, vec N0, pt P1, vec T1, vec N1, int n, int ne, float r, int col) {

    pt[] G = new pt[4];
    makePts(G);

    float d=d(P0,P1)/3;
    G[0].set(P(P0,d,U(T0)));
    G[1].set(P(P0,-d,U(T0)));
    G[2].set(P(P1,-d,U(T1)));
    G[3].set(P(P1,d,U(T1)));

    G[0].add(r,N0);
    G[1].add(r,N0);
    G[2].add(r,N1);
    G[3].add(r,N1);

    pt[] C = new pt[n];
    makePts(C);

    for(int i=0; i<n; i++) {
      C[i]=bezierPoint(G, (float) (i)/(n-1));
    }

    // displacement vectors
    vec [] L = new vec[ne];

    vec T = U(V(C[0],C[1]));
    vec LL = N(V(0,0,1),T);
    if (n2(LL)<0.01) {
      LL=N(V(1,0,0),T);
    }
    if (n2(LL)<0.01) {
      LL=N(V(0,1,0),T);
    }
    L[0]=U(N(LL,T));

    pt [][] P = new pt [2][ne];
    makePts(P[0]);
    makePts(P[1]);

    int p=0;
    boolean dark=true;

    float [] c = new float [ne];
    float [] s = new float [ne];

    for (int j=0; j<ne; j++) {
      c[j]=r*cos(TWO_PI*j/ne);
      s[j]=r*sin(TWO_PI*j/ne);
    }

    vec I0=U(L[0]);
    vec J0=U(N(L[0],T));

    for (int j=0; j<ne; j++) {
      P[p][j].set(P(P(C[0],C[1]),c[j],I0,s[j],J0));
    }

    p=1-p;

    for (int i=1; i<n-1; i++) {

      dark=!dark;

      vec I=U(V(C[i-1],C[i]));
      vec Ip=U(V(C[i],C[i+1]));
      vec IpmI=M(Ip,I);
      vec N=N(I,Ip);

      if (n(N)<0.001) {
        L[i]=V(L[i-1]);
      } else {
        L[i] = A( L[i-1] , m(U(N),I,L[i-1]) , N(U(N),M(Ip,I)) );
      }
      I=U(L[i]);
      vec J=U(N(I,Ip));

      for (int j=0; j<ne; j++) {
        P[p][j].set(P(P(C[i],C[i+1]),c[j],I,s[j],J));
      }
      p=1-p;

      for (int j=0; j<ne; j++) {
        if(dark) {
          fill(200,200,200);
        } else {
          fill(col);
        }
        dark=!dark;
        int jp=(j+ne-1)%ne;
        beginShape();
        v(P[p][jp]);
        v(P[p][j]);
        v(P[1-p][j]);
        v(P[1-p][jp]);
        endShape(CLOSE);
      }
    }
  }

}
