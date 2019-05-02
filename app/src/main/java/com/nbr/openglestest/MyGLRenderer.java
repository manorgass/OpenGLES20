package com.nbr.openglestest;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.nbr.openglestest.shapes.SimpleArrow;
import com.nbr.openglestest.shapes.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author JungWon Kim
 * @date 월요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final String TAG = this.getClass().getSimpleName();

    private Triangle mTriangle;
    private SimpleArrow simpleArrow;

    private float[] vPMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    public int highLightArrowNum = 3;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = -0.25f;
        final float eyeZ = 5.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(viewMatrix, 0,
                eyeX, eyeY, eyeZ,
                lookX, lookY, lookZ,
                upX, upY, upZ);

        //Matrix.rotateM(viewMatrix, 0, -8.5f, 1.0f, 0.0f, 0.0f);

        // Set the camera position (View matrix)
        //Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3.7f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // init a triangle
        mTriangle = new Triangle();
        simpleArrow = new SimpleArrow();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG, "onDrawFrame");

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Calculate the proejction and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.0f, 2.0f);
        //Matrix.rotateM(modelMatrix, 0, mAngle, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, mAngle, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, -90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        mTriangle.draw(mvpMatrix);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 2.5f);
        Matrix.rotateM(modelMatrix, 0, -90.0f, 1.0f, 0.0f, 0.0f);
        for (int i = 0; i < 55; i++) {
            //Matrix.rotateM(modelMatrix, 0, mAngle, 0.0f, 0.0f, 1.0f);
            //Matrix.rotateM(modelMatrix, 0, i, 0.0f, 0.0f, 1.0f);
            Matrix.translateM(modelMatrix, 0, 0.0f, 0.3f, 0.0f);
            if (i > 15) {
                Matrix.rotateM(modelMatrix, 0, mAngle, 1.0f, 0.0f, 0.0f);
            }
            //Matrix.rotateM(modelMatrix, 0, 1, 0.0f, 1.0f, 0.0f);

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
            if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                simpleArrow.draw(mvpMatrix, 0);
            } else {
                if (i % 2 == 0)
                    simpleArrow.draw(mvpMatrix, 1);
                else
                    simpleArrow.draw(mvpMatrix, 2);
            }
        }
    }

    public int highLightIndex = 0;

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 2, 100);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        Log.d(TAG, "setAngle: " + angle);
        mAngle = angle % 360;

    }
}
