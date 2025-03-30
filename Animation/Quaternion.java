package com.example.openglexemple.Animation;

import androidx.annotation.NonNull;

import com.example.openglexemple.Math.Vector3f;

public class Quaternion {

    public static final String TAG = "QUATERNION";
    public float w;
    public float x;
    public float y;
    public float z;
    private boolean normalized;

    public Quaternion(float x, float y, float z, float w ){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        normalized = false;
    }

    public Quaternion(){
        new Quaternion(0,0,0,0);
    }

    public Quaternion(Quaternion q){
        set(q.x, q.y, q.z, q.w);
        normalized = q.isNormalized();
    }

    public Quaternion(Vector3f vec, double angle){
        this.w = (float) (Math.cos(angle / 2));
        this.x = (float) (vec.x * Math.sin(angle / 2));
        this.y = (float) (vec.y * Math.sin(angle / 2));
        this.z = (float) (vec.z * Math.sin(angle / 2));
        normalize();
    }

    public Quaternion(float[] matrix){
        float[] vec = fromMatrix(matrix);
        set(vec[0], vec[1], vec[2], vec[3]);
        normalize();
    }
    public void set(float x, float y, float z, float w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    public float squareLength(){
        return x*x + y*y + z*z + w*w;
    }

    public float length(){
        return (float) Math.sqrt(squareLength());
    }

    public void normalize(){
        float l = length();
        set( x / l, y / l, z / l, w/l);
        setNormalized(true);
    }
    public Quaternion multiply(Quaternion q){
        float w = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z;
        float x = this.x * q.w + this.w * q.x - this.z * q.y + this.y * q.z;
        float y = this.y * q.w + this.z * q.x + this.w * q.y - this.x * q.z;
        float z = this.z * q.w - this.y * q.x + this.x * q.y + this.w * q.z;

        return new Quaternion(x,y,z,w);
    }

    public Quaternion multiply(float r){
        return new Quaternion(x*r, y*r, z*r,w*r);
    }

    public void divide(float r){
        this.set(x/r, y/r, z/r,w/r);
    }

    public Quaternion getDivided(float r){
        return new Quaternion( x/r, y/r, z/r,w/r);
    }

    public Quaternion getAdd(Quaternion q){
        float w = this.w + q.w;
        float x = this.x + q.x;
        float y = this.y + q.y;
        float z = this.z + q.z;
        return new Quaternion(x,y,z,w);
    }

    public void add(Quaternion q){
        set(x + q.x, y + q.y,z + q.z,w + q.w);
    }

    public Quaternion getSubtract(Quaternion q){
        float w = this.w - q.w;
        float x = this.x - q.x;
        float y = this.y - q.y;
        float z = this.z - q.z;
        return new Quaternion(x,y,z,w);
    }
    public float dot(Quaternion q){
        return x*q.x + y*q.y + z*q.z + w*q.w;
    }

    /**
     * Interpolates between two quaternion rotations and returns the resulting
     * quaternion rotation. The interpolation method here is "nlerp", or
     * "normalized-lerp". Another mnethod that could be used is "slerp", and you
     * can see a comparison of the methods here:
     * <a href="https://keithmaggio.wordpress.com/2011/02/15/math-magician-lerp-slerp-and-nlerp/">...</a>
     * <p>
     * and here:
     * <a href="http://number-none.com/product/Understanding%20Slerp,%20Then%20Not%20Using%20It/">...</a>
     *
     * @param a quaternion a
     * @param b quaternion b
     * @param blend
     *            - a value between 0 and 1 indicating how far to interpolate
     *            between the two quaternions.
     * @return The resulting interpolated rotation in quaternion format.
     */
    public static Quaternion interpolate(Quaternion a, Quaternion b, float blend) {
        Quaternion result = new Quaternion(0, 0, 0, 1);
        float dot = a.dot(b);
        float blendI = 1f - blend;
        if (dot < 0) {
            result.w = blendI * a.w + blend * -b.w;
            result.x = blendI * a.x + blend * -b.x;
            result.y = blendI * a.y + blend * -b.y;
            result.z = blendI * a.z + blend * -b.z;
        } else {
            result.w = blendI * a.w + blend * b.w;
            result.x = blendI * a.x + blend * b.x;
            result.y = blendI * a.y + blend * b.y;
            result.z = blendI * a.z + blend * b.z;
        }
        result.normalize();
        return result;
    }

    public boolean isNormalized(){
        return normalized;
    }

    public void setNormalized(boolean value){
        this.normalized = value;
    }

    public Quaternion getConjugate(){
        return new Quaternion(-this.x, -this.y, -this.z, this.w);
    }

    public float getReal(){
        return this.w;
    }

    public float[] getImaginary(){
        return new float[]{x,y,z};
    }

    public Quaternion getInverse(){
        Quaternion conj = getConjugate();
        float sl = squareLength();
        if(isNormalized()){
            return conj;
        }
        conj.divide(sl);
        return conj;
    }

    /**
     *
     * @param q
     * @return cosine of half angle between this and q
     */
    public float cosHalfAngle(Quaternion q){
        return this.w * q.w + this.x * q.x + this.y * q.y + this.z * q.z;
    }

    public float sinHalfAngle(float cos, Quaternion q){
        return (float) Math.sqrt(1 - cos * cos);
    }

    @NonNull
    public String toString() {
        return "{"+x+", "+y+", "+z+", "+w+"}";
    }

    /**
     * Converts the quaternion to a 4x4 matrix representing the exact same
     * rotation as this quaternion. (The rotation is only contained in the
     * top-left 3x3 part, but a 4x4 matrix is returned here for convenience
     * seeing as it will be multiplied with other 4x4 matrices).
     * <p>
     * More detailed explanation here:
     * <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/">...</a>
     *
     * @return The rotation matrix which represents the exact same rotation as
     *         this quaternion.
     */
    public float[] toRotationMatrix() {
        float[] matrix = new float[16];
        final float xy = x * y;
        final float xz = x * z;
        final float xw = x * w;
        final float yz = y * z;
        final float yw = y * w;
        final float zw = z * w;
        final float xx = x * x;
        final float yy = y * y;
        final float zz = z * z;
        matrix[0] = 1 - 2 * (yy + zz);
        matrix[1] = 2 * (xy - zw);
        matrix[2] = 2 * (xz + yw);
        matrix[3] = 0;

        matrix[4] = 2 * (xy + zw);
        matrix[5] = 1 - 2 * (xx + zz);
        matrix[6] = 2 * (yz - xw);
        matrix[7] = 0;

        matrix[8] = 2 * (xz - yw);
        matrix[9] = 2 * (yz + xw);
        matrix[10] = 1 - 2 * (xx + yy);
        matrix[11] = 0;

        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;
        return matrix;
    }

    /**
     * Extracts the rotation part of a transformation matrix and converts it to
     * a quaternion using the magic of maths
     * .
     * More detailed explanation here:
     * <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm">...</a>
     *
     * @param matrix
     *            - the transformation matrix containing the rotation which this
     *            quaternion shall represent.
     */
    private float[] fromMatrix(float[] matrix){
        float w, x, y, z;
        float diagonal = matrix[0] + matrix[5] + matrix[10];

        if (diagonal > 0) {
            float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
            w = w4 / 4f;
            x = (matrix[9] - matrix[6]) / w4;
            y = (matrix[2] - matrix[8]) / w4;
            z = (matrix[4] - matrix[1]) / w4;
        } else if ((matrix[0] > matrix[5]) && (matrix[0] > matrix[10])) {
            float x4 = (float) (Math.sqrt(1f + matrix[0] - matrix[5] - matrix[10]) * 2f);
            w = (matrix[9] - matrix[6]) / x4;
            x = x4 / 4f;
            y = (matrix[1] + matrix[4]) / x4;
            z = (matrix[2] + matrix[8]) / x4;
        } else if (matrix[5] > matrix[10]) {
            float y4 = (float) (Math.sqrt(1f + matrix[5] - matrix[0] - matrix[10]) * 2f);
            w = (matrix[2] - matrix[8]) / y4;
            x = (matrix[1] + matrix[4]) / y4;
            y = y4 / 4f;
            z = (matrix[6] + matrix[9]) / y4;
        } else {
            float z4 = (float) (Math.sqrt(1f + matrix[10] - matrix[0] - matrix[5]) * 2f);
            w = (matrix[4] - matrix[1]) / z4;
            x = (matrix[2] + matrix[8]) / z4;
            y = (matrix[6] + matrix[9]) / z4;
            z = z4 / 4f;
        }
        return new float[]{x, y, z, w};
    }
}
