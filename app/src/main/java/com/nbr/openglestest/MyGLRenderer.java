package com.nbr.openglestest;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.nbr.openglestest.shapes.LineDot;
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
    private LineDot lineDot;

    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    private float[] highlightArrowModelMatrix = new float[16];
    private float[] highlightArrowMvpMatrix = new float[16];

    public int highLightArrowNum = 3;
    public int highLightIndex = -1;

    public int arrowNum = 80;

    public float arrowRotationAxis_x = 0.0f;
    public float arrowRotationAxis_y = 1.0f;
    public float arrowRotationAxis_z = 0.0f;

    public int rotationStartIndex = 10;

    public int maxAngle = 190;

    public float eyeY = 0.25f;
    public float eyeZ = 3.4f;

    public static final int DRAW_NORMAL = 0;
    public static final int DRAW_Z_SPIN = 1;
    public static final int DRAW_LINE_DOT = 2;
    public static final int DRAW_LINE_CROSS = 3;
    public static final int DRAW_LINE_TRIANGLE = 4;
    int drawMode = 0;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        //GLES20.glEnable(GLES20.GL_BLEND);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;

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

        // init a triangle
        mTriangle = new Triangle();
        simpleArrow = new SimpleArrow();
        lineDot = new LineDot();

        // init highlight arrow coords
        setHighlightArrow();
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG, "onDrawFrame");

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

       /* Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.0f, 2.0f);
        //Matrix.rotateM(modelMatrix, 0, mAngle, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, mAngle, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, -90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        mTriangle.drawDots(mvpMatrix);*/


        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);

        float[] rightLineCoords = new float[arrowNum * 3];
        float[] leftLineCoords = new float[arrowNum * 3];


        int coordsBufferIndex = 0;
        float baseX = 0;
        float baseZ = 0;
        float yAxisDegree = 0;
        float drawStride = -0.3f;

        // drawDots arrow
        switch (drawMode) {
            case DRAW_NORMAL:
                for (int i = 0; i < arrowNum; i++) {
                    //Matrix.rotateM(modelMatrix, 0, mAngle, 0.0f, 0.0f, 1.0f);
                    //Matrix.rotateM(modelMatrix, 0, i, 0.0f, 0.0f, 1.0f);
                    Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -0.3f);
                    if (i > rotationStartIndex
                            && ((i - rotationStartIndex) * mAngle < maxAngle && (i - rotationStartIndex) * mAngle > -maxAngle)) {
                        Matrix.rotateM(modelMatrix, 0, mAngle, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
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
                break;
            case DRAW_Z_SPIN: // 좌회전
                for (int i = 0; i < arrowNum; i++) {
                    Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -0.3f);
                    if (i > rotationStartIndex) {
                        if (((i - rotationStartIndex) * mAngle < 100 && (i - rotationStartIndex) * mAngle > -100)) {
                            Matrix.rotateM(modelMatrix, 0, mAngle, (float) Math.sin(Math.toRadians(zAxisRotationDegree)), (float) Math.cos(Math.toRadians(zAxisRotationDegree)), 0.0f);

                        }
                        //if (zAxisRotationDegree < 90)

                        switch (flag) {
                            case 0: // up
                                Matrix.rotateM(modelMatrix, 0, zAxisRotationStride, 0.0f, 0.0f, 1.0f);
                                zAxisRotationDegree += zAxisRotationStride;
                                break;
                            case 1: // down
                                Matrix.rotateM(modelMatrix, 0, -zAxisRotationStride, 0.0f, 0.0f, 1.0f);
                                zAxisRotationDegree -= zAxisRotationStride;
                                break;
                        }

                        if (zAxisRotationDegree > degreeLimit) flag = 1;
                        else if (zAxisRotationDegree <= 0) flag = 2;

                    }

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
                flag = 0;
                break;
            case DRAW_LINE_DOT:
                rightLineCoords = new float[arrowNum * 3];
                leftLineCoords = new float[arrowNum * 3];
                coordsBufferIndex = 0;
                baseX = 0;
                baseZ = 0;
                yAxisDegree = 0;
                drawStride = -0.3f;

                // 시작점은 0,0
                /*leftLineCoords[coordsBufferIndex] = 0.0f;
                leftLineCoords[coordsBufferIndex + 1] = 0.0f;
                leftLineCoords[coordsBufferIndex + 2] = 0.0f;
                coordsBufferIndex += 3;*/

                for (int i = 0; i < arrowNum; i++) {
                    if (i > rotationStartIndex && ((i - rotationStartIndex) * mAngle < maxAngle && (i - rotationStartIndex) * mAngle > -maxAngle)) {
                        Matrix.rotateM(modelMatrix, 0, mAngle, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                        // 회전 각도 계산
                        yAxisDegree += mAngle;
                        yAxisDegree %= 360;
                    }

                    // line coords 계산
                    if (coordsBufferIndex == rightLineCoords.length) break;
                    // x axis position
                    rightLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + yAxisDegree)));
                    leftLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + yAxisDegree)));
                    // y axis position (초기값이 0이므로 굳이 값을 대입하지 않음)
                    // rightLineCoords[coordsBufferIndex + 1] = 0;
                    // z axis position
                    rightLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + yAxisDegree)));
                    leftLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + yAxisDegree)));


                    baseX += drawStride * (float) Math.sin(Math.toRadians(yAxisDegree));
                    baseZ += drawStride * (float) Math.cos(Math.toRadians(yAxisDegree));

                    // index 증가
                    coordsBufferIndex += 3;

                    Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                    if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                        lineDot.drawDots(mvpMatrix, 0);
                    } else {
                        if (i % 2 == 0) lineDot.drawDots(mvpMatrix, 1);
                        else lineDot.drawDots(mvpMatrix, 2);
                    }

                    Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, drawStride);
                }

                lineDot.setLineVertexBuffer(leftLineCoords);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                lineDot.drawLines(mvpMatrix);

                lineDot.setLineVertexBuffer(rightLineCoords);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                lineDot.drawLines(mvpMatrix);
                break;
            case DRAW_LINE_CROSS:
                rightLineCoords = new float[arrowNum * 3];
                leftLineCoords = new float[arrowNum * 3];

                coordsBufferIndex = 0;
                baseX = 0;
                baseZ = 0;
                yAxisDegree = 0;
                drawStride = -0.3f;

                int vertexStride = 3;
                int crossPerVertexNumber = 4;
                int crossCount = ((int) (lineStride * 10) - 1) * 2 + 1;
                float[] crossCoords = new float[crossCount * crossPerVertexNumber * vertexStride];
                for (int i = ((int) (lineStride * 10) - 1) * -1; i < (int) (lineStride * 10); i++) {
                    crossCoords[coordsBufferIndex] = (float) i / 10.0f + 0.025f;
                    crossCoords[coordsBufferIndex + 1] = 0.0f;
                    crossCoords[coordsBufferIndex + 2] = 0.0f;

                    crossCoords[coordsBufferIndex + 3] = (float) i / 10.0f - 0.025f;
                    crossCoords[coordsBufferIndex + 4] = 0.0f;
                    crossCoords[coordsBufferIndex + 5] = 0.0f;

                    crossCoords[coordsBufferIndex + 6] = (float) i / 10.0f;
                    crossCoords[coordsBufferIndex + 7] = 0.0f;
                    crossCoords[coordsBufferIndex + 8] = 0.025f;

                    crossCoords[coordsBufferIndex + 9] = (float) i / 10.0f;
                    crossCoords[coordsBufferIndex + 10] = 0.0f;
                    crossCoords[coordsBufferIndex + 11] = -0.025f;

                    coordsBufferIndex += vertexStride * crossPerVertexNumber;
                }

                lineDot.setCrossVertexBuffer(crossCoords);
                coordsBufferIndex = 0;
                for (int i = 0; i < arrowNum; i++) {
                    if (i > rotationStartIndex && ((i - rotationStartIndex) * mAngle < maxAngle && (i - rotationStartIndex) * mAngle > -maxAngle)) {
                        Matrix.rotateM(modelMatrix, 0, mAngle, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                        // 회전 각도 계산
                        yAxisDegree += mAngle;
                        yAxisDegree %= 360;
                    }

                    // line coords 계산
                    if (coordsBufferIndex == rightLineCoords.length) break;
                    rightLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + yAxisDegree)));
                    leftLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + yAxisDegree)));
                    rightLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + yAxisDegree)));
                    leftLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + yAxisDegree)));


                    baseX += drawStride * (float) Math.sin(Math.toRadians(yAxisDegree));
                    baseZ += drawStride * (float) Math.cos(Math.toRadians(yAxisDegree));

                    // index 증가
                    coordsBufferIndex += 3;


                    Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                    if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                        lineDot.drawCross(mvpMatrix, 0);
                    } else {
                        if (i % 2 == 0) lineDot.drawCross(mvpMatrix, 1);
                        else lineDot.drawCross(mvpMatrix, 2);
                    }

                    Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, drawStride);
                }

                lineDot.setLineVertexBuffer(leftLineCoords);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                lineDot.drawLines(mvpMatrix);

                lineDot.setLineVertexBuffer(rightLineCoords);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                lineDot.drawLines(mvpMatrix);
                break;

            case DRAW_LINE_TRIANGLE:
                rightLineCoords = new float[arrowNum * 3];
                leftLineCoords = new float[arrowNum * 3];

                coordsBufferIndex = 0;
                baseX = 0;
                baseZ = 0;
                yAxisDegree = 0;
                drawStride = -0.3f;

                zSpinSum = 0;

                float triangleScale = 0.025f;
                vertexStride = 3;
                int trianglePerVertexCount = 6;
                int triangleCount = ((int) (lineStride * 10) - 1) * 2 + 1;
                float[] triangleCoords = new float[triangleCount * trianglePerVertexCount * vertexStride];
                for (int i = ((int) (lineStride * 10) - 1) * -1; i < (int) (lineStride * 10); i++) {
                    triangleCoords[coordsBufferIndex + 0] = (float) i / 10.0f + triangleScale;
                    triangleCoords[coordsBufferIndex + 1] = 0.0f;
                    triangleCoords[coordsBufferIndex + 2] = triangleScale;

                    triangleCoords[coordsBufferIndex + 3] = (float) i / 10.0f - triangleScale;
                    triangleCoords[coordsBufferIndex + 4] = 0.0f;
                    triangleCoords[coordsBufferIndex + 5] = triangleScale;

                    triangleCoords[coordsBufferIndex + 6] = (float) i / 10.0f - triangleScale;
                    triangleCoords[coordsBufferIndex + 7] = 0.0f;
                    triangleCoords[coordsBufferIndex + 8] = triangleScale;

                    triangleCoords[coordsBufferIndex + 9] = (float) i / 10.0f;
                    triangleCoords[coordsBufferIndex + 10] = 0.0f;
                    triangleCoords[coordsBufferIndex + 11] = -triangleScale;

                    triangleCoords[coordsBufferIndex + 12] = (float) i / 10.0f;
                    triangleCoords[coordsBufferIndex + 13] = 0.0f;
                    triangleCoords[coordsBufferIndex + 14] = -triangleScale;

                    triangleCoords[coordsBufferIndex + 15] = (float) i / 10.0f + triangleScale;
                    triangleCoords[coordsBufferIndex + 16] = 0.0f;
                    triangleCoords[coordsBufferIndex + 17] = triangleScale;

                    coordsBufferIndex += vertexStride * trianglePerVertexCount;
                }

                lineDot.setTriangleVertexBuffer(triangleCoords);
                coordsBufferIndex = 0;
                for (int i = 0; i < arrowNum; i++) {
                    if (i > rotationStartIndex && ((i - rotationStartIndex) * mAngle < maxAngle && (i - rotationStartIndex) * mAngle > -maxAngle)) {
                        Matrix.rotateM(modelMatrix, 0, mAngle, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);

                        // 하이라이트 화살표 메트릭스 초기화
                        System.arraycopy(modelMatrix, 0, highlightArrowModelMatrix, 0, 16);
                        // 최대 회전 각보다 작은 경우 stride 만큼 z축으로 회전. 사용자가 지정한 앵글의 부호에 따라 + / - 를 정해준다.
                        if (zSpinSum < zSpinLimit) {
                            if (mAngle > 0)
                                Matrix.rotateM(highlightArrowModelMatrix, 0, zSpintStride, 0.0f, 0.0f, 1.0f);
                            else
                                Matrix.rotateM(highlightArrowModelMatrix, 0, -zSpintStride, 0.0f, 0.0f, 1.0f);
                            zSpinSum += zSpintStride;
                        }

                        // 회전 각도 계산
                        yAxisDegree += mAngle;
                        yAxisDegree %= 360;
                    } else {
                        System.arraycopy(modelMatrix, 0, highlightArrowModelMatrix, 0, 16);
                    }

                    // line coords 계산
                    if (coordsBufferIndex == rightLineCoords.length) break;
                    rightLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + yAxisDegree)));
                    leftLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + yAxisDegree)));
                    rightLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + yAxisDegree)));
                    leftLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + yAxisDegree)));


                    baseX += drawStride * (float) Math.sin(Math.toRadians(yAxisDegree));
                    baseZ += drawStride * (float) Math.cos(Math.toRadians(yAxisDegree));

                    // index 증가
                    coordsBufferIndex += 3;


                    Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                    if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                        lineDot.drawTriangle(mvpMatrix, 0);
                        Matrix.multiplyMM(highlightArrowMvpMatrix, 0, viewMatrix, 0, highlightArrowModelMatrix, 0);
                        Matrix.multiplyMM(highlightArrowMvpMatrix, 0, projectionMatrix, 0, highlightArrowMvpMatrix, 0);
                        lineDot.drawHighLightArrow(highlightArrowMvpMatrix);
                    } else {
                        if (i % 2 == 0) lineDot.drawTriangle(mvpMatrix, 1);
                        else lineDot.drawTriangle(mvpMatrix, 2);
                    }

                    Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, drawStride);
                }

                lineDot.setLineVertexBuffer(leftLineCoords);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                lineDot.drawLines(mvpMatrix);

                lineDot.setLineVertexBuffer(rightLineCoords);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                lineDot.drawLines(mvpMatrix);
                break;
        }
    }

    public float lineStride = 1.3f;

    public void setHighlightArrow() {
        float yAxis = 0.15f; //해당 값만큼 위에 떠있음.
        float[] highlightArrowCoords = new float[]{
                0.0f, yAxis, -0.7f,
                (lineStride - 0.3f), yAxis, 0.0f,
                (lineStride - 0.3f), yAxis, -0.2f,
                0.0f, yAxis, -1.0f,
                -(lineStride - 0.3f), yAxis, -0.2f,
                -(lineStride - 0.3f), yAxis, 0.0f,
        };
        lineDot.setHighLightArrowVertexBuffer(highlightArrowCoords);
    }


    int flag;
    float zAxisRotationDegree = 0;
    public float zAxisRotationStride = 1;
    public int degreeLimit = 30;

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 2, 50.0f);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    public float zSpinLimit = 0;
    public float zSpintStride = 3.0f;
    public float zSpinSum = 0;

    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        Log.d(TAG, "setAngle: " + angle);
        mAngle = angle % 360;

    }


    public void updateCameraPosition() {
        // Position the eye in front of the origin.
        final float eyeX = 0.0f;

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
    }
}
