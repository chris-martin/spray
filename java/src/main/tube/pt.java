package tube;

// point
public class pt {

    public float x, y, z;

    public pt() {
    }

    public pt(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
    }

    public pt set(float px, float py, float pz) {
        x = px;
        y = py;
        z = pz;
        return this;
    }

    public pt set(pt P) {
        x = P.x;
        y = P.y;
        z = P.z;
        return this;
    }

    public pt add(pt P) {
        x += P.x;
        y += P.y;
        z += P.z;
        return this;
    }

    public pt add(vec V) {
        x += V.x;
        y += V.y;
        z += V.z;
        return this;
    }

    public pt add(float s, vec V) {
        x += s * V.x;
        y += s * V.y;
        z += s * V.z;
        return this;
    }

    public pt sub(pt P) {
        x -= P.x;
        y -= P.y;
        z -= P.z;
        return this;
    }

    public pt mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public pt div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public pt div(int f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

}
