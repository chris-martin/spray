package spray;

import org.testng.annotations.Test;
import spray.Geometry.Vec3;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static spray.Geometry.origin3;
import static spray.Geometry.xyz;

public class MeshTest {

    @Test
    public void test1() {
        Mesh mesh = new Mesh();
        mesh.rollingScale = 3;
        mesh.setBalls(new Balls<Vec3>(Arrays.<Vec3>asList(
            xyz(1, 0, 0),
            xyz(0, 1, 0),
            xyz(0, 0, 1)
        )));
        assertEquals(mesh.triangles.size(), 2);
    }

    @Test
    public void test2() {
        Mesh mesh = new Mesh();
        mesh.rollingScale = 3;
        mesh.setBalls(new Balls<Vec3>(Arrays.<Vec3>asList(
            xyz(1, 0, 0),
            xyz(0, 1, 0),
            xyz(0, 0, 1),
            origin3()
        )));
        assertEquals(mesh.triangles.size(), 4);
    }

    @Test
    public void test3() {
        Mesh mesh = new Mesh();
        mesh.rollingScale = 10;
        mesh.setBalls(new Balls<Vec3>(Arrays.<Vec3>asList(
            xyz(1, 0, 0),
            xyz(0, 1, 0),
            xyz(0, 0, 1),
            xyz(.2, .2, .2),
            origin3()
        )));
        assertEquals(mesh.triangles.size(), 4);
    }

}
