import org.joml.*;
public class JomlMatrixParseTest {
    public static void main(String[] args) {
        float[] arr = {
            0.9651f, 0f, 0f, -0.4825f,
            0f, -1.127502266354306f, 0.12282566880296407f, 0.903f,
            0f, -0.11057689101589653f, -1.2523974825980284f, 0.8849f,
            0f, 0f, 0f, 1f
        };
        // Construct assuming row-major JSON -> correctly place into column-major joML
        Matrix4f matRowMajorToJoml = new Matrix4f(
            arr[0], arr[4], arr[8], arr[12], // Col 0
            arr[1], arr[5], arr[9], arr[13], // Col 1
            arr[2], arr[6], arr[10], arr[14], // Col 2
            arr[3], arr[7], arr[11], arr[15]  // Col 3
        );
        
        System.out.println("Parsed Trans: " + matRowMajorToJoml.getTranslation(new Vector3f()));
        System.out.println("Parsed Scale: " + matRowMajorToJoml.getScale(new Vector3f()));
        System.out.println("Parsed UnnormalizedRot: " + matRowMajorToJoml.getUnnormalizedRotation(new Quaternionf()));
        System.out.println("Parsed NormalizedRot: " + matRowMajorToJoml.getNormalizedRotation(new Quaternionf()));
        
        // Manual math from ModelLoader.java:
        float m00 = arr[0], m01 = arr[1], m02 = arr[2], m03 = arr[3];
        float m10 = arr[4], m11 = arr[5], m12 = arr[6], m13 = arr[7];
        float m20 = arr[8], m21 = arr[9], m22 = arr[10], m23 = arr[11];
        
        Vector3f translation = new Vector3f(m03, m13, m23);
        float sx = (float) java.lang.Math.sqrt(m00*m00 + m10*m10 + m20*m20);
        float sy = (float) java.lang.Math.sqrt(m01*m01 + m11*m11 + m21*m21);
        float sz = (float) java.lang.Math.sqrt(m02*m02 + m12*m12 + m22*m22);
        
        float det = m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m12 * m20) + m02 * (m10 * m21 - m11 * m20);
        if (det < 0) sx = -sx;
        
        float isx = sx != 0 ? 1f / sx : 0;
        float isy = sy != 0 ? 1f / sy : 0;
        float isz = sz != 0 ? 1f / sz : 0;
        
        org.joml.Matrix3f rot = new org.joml.Matrix3f(
            m00 * isx, m10 * isx, m20 * isx,
            m01 * isy, m11 * isy, m21 * isy,
            m02 * isz, m12 * isz, m22 * isz
        );
        Quaternionf leftRot = new Quaternionf().setFromNormalized(rot);
        
        System.out.println("Manual Trans: " + translation);
        System.out.println("Manual Scale: " + new Vector3f(sx, sy, sz));
        System.out.println("Manual Rot: " + leftRot);
    }
}
