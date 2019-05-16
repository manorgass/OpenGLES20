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
public class TBTDrawer {
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

    public static final int MATERIAL_TYPE_DOTS = 0;
    public static final int MATERIAL_TYPE_CROSS = 1;
    public static final int MATERIAL_TYPE_TRIANGLE = 2;
    public static final int MATERIAL_TYPE_ARROW = 3;

    private FloatBuffer highlightArrowVertexBuffer;
    private FloatBuffer materialVertexBuffer;
    private FloatBuffer linesVertexBuffer;

    private final int program;

    private int mvpMatrixHandle;
    private static final int COORDS_PER_VERTEX = 3;
    private int materialVertexCount;
    private int lineVertexCount;
    public static int LINE_WIDTH = 1;

    ///private float colorWhite[] = {1.0f, 1.0f, 1.0f, 0.99f};
    public float colorMaterialPattern1[] = {0.87843137f, 0.949019f, 0.945098039f, 0.4f};
    public float colorMaterialPattern2[] = {0.4117647f, 0.9411764f, 0.6823529f, 0.4f};
    public float colorLine[] = {0.0f, 1.0f, 0.0f, 0.3f};
    public float colorHighlight[] = {0.0f, 0.7843137254901961f, 0.3254901960784314f, 0.5f};
    private int positionHandle;
    private int colorHandle;

    private final int vertexStride = COORDS_PER_VERTEX * Float.BYTES;

    public TBTDrawer() {
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    public void drawMaterial(float[] mvpMatrix, int colorSelector, int materialType) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, materialVertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");

        switch (colorSelector) {
            case 0:
                GLES20.glUniform4fv(colorHandle, 1, colorHighlight, 0);
                break;
            case 1:
                GLES20.glUniform4fv(colorHandle, 1, colorMaterialPattern1, 0);
                break;
            case 2:
                GLES20.glUniform4fv(colorHandle, 1, colorMaterialPattern2, 0);
                break;
        }

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        switch (materialType) {
            case MATERIAL_TYPE_CROSS:
            case MATERIAL_TYPE_TRIANGLE:
                GLES20.glLineWidth(LINE_WIDTH);
                GLES20.glDrawArrays(GLES20.GL_LINES, 0, materialVertexCount);
                break;
            case MATERIAL_TYPE_DOTS:
                GLES20.glDrawArrays(GLES20.GL_POINTS, 0, materialVertexCount);
                break;
            case MATERIAL_TYPE_ARROW:
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, materialVertexCount);
                break;
        }

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

    public void setMaterialCoords(float[] materialCoords) {
        ByteBuffer crossBuffer = ByteBuffer.allocateDirect(materialCoords.length * Float.BYTES);
        crossBuffer.order(ByteOrder.nativeOrder());
        materialVertexBuffer = crossBuffer.asFloatBuffer();
        materialVertexBuffer.put(materialCoords);
        materialVertexBuffer.position(0);

        materialVertexCount = materialCoords.length / 3;
    }


    public void drawLine(float[] mvpMatrix) {
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
        GLES20.glUniform4fv(colorHandle, 1, colorHighlight, 0);

        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
