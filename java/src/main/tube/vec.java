package tube;

import static processing.core.PApplet.*;

// vector
public class vec {

    public float x, y, z;

    public vec() {
    }

    public vec(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
    }

    public vec set(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
        return this;
    }

    public vec set(vec V) {
        x = V.x;
        y = V.y;
        z = V.z;
        return this;
    }

    public vec add(vec V) {
        x += V.x;
        y += V.y;
        z += V.z;
        return this;
    }

    public vec add(float s, vec V) {
        x += s * V.x;
        y += s * V.y;
        z += s * V.z;
        return this;
    }

    public vec sub(vec V) {
        x -= V.x;
        y -= V.y;
        z -= V.z;
        return this;
    }

    public vec mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public vec div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public vec div(int f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public vec rev() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public float norm() {
        return (sqrt(sq(x) + sq(y) + sq(z)));
    }

    public vec normalize() {
        float n = norm();
        if (n > 0.000001) {
            div(n);
        }
        return this;
    }

    // Rotate by a parallel to plane (I,J)
    public vec rotate(float a, vec I, vec J) {
        float x = Main.d(this, I), y = Main.d(this, J);
        float c = cos(a), s = sin(a);
        add(x * c - x - y * s, I);
        add(x * s + y * c - y, J);
        return this;
    }

}
