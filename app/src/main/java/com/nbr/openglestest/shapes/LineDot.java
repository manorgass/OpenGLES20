package com.nbr.openglestest.shapes;


import android.opengl.GLES20;
import android.util.Log;

import com.nbr.openglestest.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author JungWon Kim
 * @date 수요일, 5월, 2019
 * @email manorgass@gmail.com
 */
public class LineDot {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;                   " +
                    "attribute vec4 vPosition;                  " +
                    "void main() {                              " +
                    "   gl_Position = uMVPMatrix * vPosition;   " +
                    "   gl_PointSize = 3.0;   " +
                    "}                                          ";

    private final String fragmentShaderCode =
            "precision mediump float;         " +
                    "uniform vec4 vColor;              " +
                    "void main() {                    " +
                    "   gl_FragColor = vColor;        " +
                    "}                                ";

    private FloatBuffer highlightArrowVertexBuffer;
    private FloatBuffer dotsVertexBuffer;
    private FloatBuffer crossVertexBuffer;
    private FloatBuffer triangleVertexBuffer;
    private FloatBuffer linesVertexBuffer;

    private final int program;

    private int mvpMatrixHandle;
    private static final float dotScale = 1.0f;
    private static final int COORDS_PER_VERTEX = 3;

    private static float dotCoords[] = {
            -1.0f * dotScale, 0.0f, 0.0f,
            -0.9f * dotScale, 0.0f, 0.0f,
            -0.8f * dotScale, 0.0f, 0.0f,
            -0.7f * dotScale, 0.0f, 0.0f,
            -0.6f * dotScale, 0.0f, 0.0f,
            -0.5f * dotScale, 0.0f, 0.0f,
            -0.4f * dotScale, 0.0f, 0.0f,
            -0.3f * dotScale, 0.0f, 0.0f,
            -0.2f * dotScale, 0.0f, 0.0f,
            -0.1f * dotScale, 0.0f, 0.0f,
            0.0f * dotScale, 0.0f, 0.0f,
            0.1f * dotScale, 0.0f, 0.0f,
            0.2f * dotScale, 0.0f, 0.0f,
            0.3f * dotScale, 0.0f, 0.0f,
            0.4f * dotScale, 0.0f, 0.0f,
            0.5f * dotScale, 0.0f, 0.0f,
            0.6f * dotScale, 0.0f, 0.0f,
            0.7f * dotScale, 0.0f, 0.0f,
            0.8f * dotScale, 0.0f, 0.0f,
            0.9f * dotScale, 0.0f, 0.0f,
            1.0f * dotScale, 0.0f, 0.0f,
    };

    private static float crossCoords[] = {
            0.025f, 0.0f, 0.0f,
            -0.025f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.025f,
            0.0f, 0.0f, -0.025f,
    };

    private static float lineCoords[] = {
            -1.3f * dotScale, 0.0f, 0.03f * dotScale,
            -1.3f * dotScale, 0.0f, 0.33f * dotScale,
            1.3f * dotScale, 0.0f, 0.03f * dotScale,
            1.3f * dotScale, 0.0f, 0.33f * dotScale
    };

    ///private float colorWhite[] = {1.0f, 1.0f, 1.0f, 0.99f};
    public float color[] = {0.87843137f, 0.949019f, 0.945098039f, 0.4f};
    public float color1[] = {0.4117647f, 0.9411764f, 0.6823529f, 0.4f};
    public float colorShadow[] = {0.0f, 1.0f, 0.0f, 0.7f};
    public float colorLine[] = {0.0f, 1.0f, 0.0f, 0.3f};
    public float colorHighlightArrow[] = {0.0f, 0.7843137254901961f, 0.3254901960784314f, 0.5f};

    public LineDot() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(dotCoords.length * Float.BYTES);
        buffer.order(ByteOrder.nativeOrder());
        dotsVertexBuffer = buffer.asFloatBuffer();
        dotsVertexBuffer.put(dotCoords);
        dotsVertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(crossCoords.length * Float.BYTES);
        buffer.order(ByteOrder.nativeOrder());
        crossVertexBuffer = buffer.asFloatBuffer();
        crossVertexBuffer.put(crossCoords);
        crossVertexBuffer.position(0);


        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    private int positionHandle;
    private int colorHandle;

    private final int vertexCount = dotCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * Float.BYTES;

    public void drawCross(float[] mvpMatrix, int colorSelector) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, crossVertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");

        switch (colorSelector) {
            case 0:
                GLES20.glUniform4fv(colorHandle, 1, colorHighlightArrow, 0);
                break;
            case 1:
                GLES20.glUniform4fv(colorHandle, 1, color, 0);
                break;
            case 2:
                GLES20.glUniform4fv(colorHandle, 1, color1, 0);
                break;
        }

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(1);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, crossVertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void drawTriangle(float[] mvpMatrix, int colorSelector) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, triangleVertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");

        switch (colorSelector) {
            case 0:
                GLES20.glUniform4fv(colorHandle, 1, colorHighlightArrow, 0);
                break;
            case 1:
                GLES20.glUniform4fv(colorHandle, 1, color, 0);
                break;
            case 2:
                GLES20.glUniform4fv(colorHandle, 1, color1, 0);
                break;
        }

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(1);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, triangleVertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }


    public void drawDots(float[] mvpMatrix, int colorSelector) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, dotsVertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");

        switch (colorSelector) {
            case 0:
                GLES20.glUniform4fv(colorHandle, 1, colorHighlightArrow, 0);
                break;
            case 1:
                GLES20.glUniform4fv(colorHandle, 1, color, 0);
                break;
            case 2:
                GLES20.glUniform4fv(colorHandle, 1, color1, 0);
                break;
        }

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, dotsVertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void setLineVertexBuffer(float[] lineCoords) {
        ByteBuffer lineBuffer = ByteBuffer.allocateDirect(lineCoords.length * Float.BYTES);
        lineBuffer.order(ByteOrder.nativeOrder());
        linesVertexBuffer = lineBuffer.asFloatBuffer();
        linesVertexBuffer.put(lineCoords);
        linesVertexBuffer.position(0);

        lineVertexCount = lineCoords.length / 3;
    }

    public void setDotsVertexBuffer(float[] dotsCoords) {
        ByteBuffer crossBuffer = ByteBuffer.allocateDirect(dotsCoords.length * Float.BYTES);
        crossBuffer.order(ByteOrder.nativeOrder());
        dotsVertexBuffer = crossBuffer.asFloatBuffer();
        dotsVertexBuffer.put(dotsCoords);
        dotsVertexBuffer.position(0);

        dotsVertexCount = dotsCoords.length / 3;
    }

    public void setCrossVertexBuffer(float[] crossCoords) {
        ByteBuffer crossBuffer = ByteBuffer.allocateDirect(crossCoords.length * Float.BYTES);
        crossBuffer.order(ByteOrder.nativeOrder());
        crossVertexBuffer = crossBuffer.asFloatBuffer();
        crossVertexBuffer.put(crossCoords);
        crossVertexBuffer.position(0);

        crossVertexCount = crossCoords.length / 3;
    }

    public void setTriangleVertexBuffer(float[] triangleCoords) {
        ByteBuffer crossBuffer = ByteBuffer.allocateDirect(triangleCoords.length * Float.BYTES);
        crossBuffer.order(ByteOrder.nativeOrder());
        triangleVertexBuffer = crossBuffer.asFloatBuffer();
        triangleVertexBuffer.put(triangleCoords);
        triangleVertexBuffer.position(0);

        triangleVertexCount = triangleCoords.length / 3;
    }

    private int triangleVertexCount;
    private int crossVertexCount;
    private int dotsVertexCount;
    private int lineVertexCount;
    public static int LINE_WIDTH = 5;

    public void drawLines(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, linesVertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, colorLine, 0);

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(LINE_WIDTH);
        Log.e("lineDot", "lineWidth" + LINE_WIDTH);

        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, lineVertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void setHighLightArrowVertexBuffer(float[] highlightArrowCoords) {
        ByteBuffer crossBuffer = ByteBuffer.allocateDirect(highlightArrowCoords.length * Float.BYTES);
        crossBuffer.order(ByteOrder.nativeOrder());
        highlightArrowVertexBuffer = crossBuffer.asFloatBuffer();
        highlightArrowVertexBuffer.put(highlightArrowCoords);
        highlightArrowVertexBuffer.position(0);

    }

    public void drawHighLightArrow(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, highlightArrowVertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, colorHighlightArrow, 0);

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
