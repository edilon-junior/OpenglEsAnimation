package com.example.openglexemple.Animation;

import android.opengl.Matrix;

import com.example.openglexemple.Math.Vector3f;

import java.util.Arrays;

/**
 *
 * Represents the local bone-space transform of a joint at a certain keyframe
 * during an animation. This includes the position and rotation of the joint,
 * relative to the parent joint (for the root joint it's relative to the model's
 * origin, seeing as the root joint has no parent). The transform is stored as a
 * position vector and a quaternion (rotation) so that these values can be
 * easily interpolated, a functionality that this class also provides.
 *
 * @author Karl
 *
 */
public class JointTransform {

    private final Vector3f translation;
    private final Quaternion rotation;

    /**
     *
     * @param translation
     *            - the position of the joint relative to the parent joint
     *            (bone-space) at a certain keyframe. For example, if this joint
     *            is at (5, 12, 0) in the model's coordinate system, and the
     *            parent of this joint is at (2, 8, 0), then the position of
     *            this joint relative to the parent is (3, 4, 0).
     * @param rotation
     *            - the rotation of the joint relative to the parent joint
     *            (bone-space) at a certain keyframe.
     */
    public JointTransform(Vector3f translation, Quaternion rotation){
        this.translation = translation;
        this.rotation = rotation;
    }

    /**
     * In this method the bone-space transform matrix is constructed by
     * translating an identity matrix using the position variable and then
     * applying the rotation. The rotation is applied by first converting the
     * quaternion into a rotation matrix, which is then multiplied with the
     * transform matrix.
     *
     * @return This bone-space joint transform as a matrix. The exact same
     *         transform as represented by the position and rotation in this
     *         instance, just in matrix form.
     */
    protected float[] getLocalTransform() {
        float[] transformMatrix = new float[16];
        float[] translateMatrix = new float[16];
        Matrix.setIdentityM(translateMatrix,0);
        Matrix.translateM(translateMatrix,0,translation.x, translation.y, translation.z);
        Matrix.multiplyMM(transformMatrix,0,translateMatrix, 0,  rotation.toRotationMatrix(), 0);
        return transformMatrix;
    }

    /**
     * Interpolates between two transforms based on the progression value. The
     * result is a new transform which is part way between the two original
     * transforms. The translation can simply be linearly interpolated, but the
     * rotation interpolation is slightly more complex, using a method called
     * "SLERP" to spherically-linearly interpolate between 2 quaternions
     * (rotations). This gives a much much better result than trying to linearly
     * interpolate between Euler rotations.
     *
     * @param frameA
     *            - the previous transform
     * @param frameB
     *            - the next transform
     * @param progression
     *            - a number between 0 and 1 indicating how far between the two
     *            transforms to interpolate. A progression value of 0 would
     *            return a transform equal to "frameA", a value of 1 would
     *            return a transform equal to "frameB". Everything else gives a
     *            transform somewhere in-between the two.
     * @return intermediary transform
     */
    protected static JointTransform interpolate(JointTransform frameA, JointTransform frameB, float progression) {
        Vector3f pos = interpolate(frameA.translation, frameB.translation, progression);
        Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression);
        return new JointTransform(pos, rot);
    }

    /**
     * Linearly interpolates between two translations based on a "progression"
     * value.
     *
     * @param start
     *            - the start translation.
     * @param end
     *            - the end translation.
     * @param progression
     *            - a value between 0 and 1 indicating how far to interpolate
     *            between the two translations.
     * @return
     */
    private static Vector3f interpolate(Vector3f start, Vector3f end, float progression) {
        float x = start.x + (end.x - start.x) * progression;
        float y = start.y + (end.y - start.y) * progression;
        float z = start.z + (end.z - start.z) * progression;
        return new Vector3f(x, y, z);
    }

    @Override
    public String toString(){
        return Arrays.toString(getLocalTransform());
    }
}
