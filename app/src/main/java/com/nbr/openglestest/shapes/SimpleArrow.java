package com.nbr.openglestest.shapes;


import android.opengl.GLES20;

import com.nbr.openglestest.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author JungWon Kim
 * @date 화요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class SimpleArrow {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;                   " +
                    "attribute vec4 vPosition;                  " +
                    "void main() {                              " +
                    "   gl_Position = uMVPMatrix * vPosition;   " +
                    "}                                          ";

    private final String fragmentShaderCode =
            "precision mediump float;         " +
                    "uniform vec4 vColor;              " +
                    "void main() {                    " +
                    "   gl_FragColor = vColor;        " +
                    "}                                ";

    private FloatBuffer vertexBuffer;

    private final int program;

    private int mvpMatrixHandle;
    private static final float xScale = 3.0f;
    private static final float yScale = 8.0f;
    private static final int COORDS_PER_VERTEX = 3;
    private static float arrowCoords[] = {
            // 좌측 상단 삼각형
            0, 0.5f * yScale, 0,
            -0.5f * xScale, 0.2f * yScale, 0,
            0, 0.3f * yScale, 0,

            // 좌측 하단 삼각형
            0, 0.3f * yScale, 0,
            -0.5f * xScale, 0.2f * yScale, 0,
            -0.5f * xScale, 0, 0,

            // 우측 상단 삼각형
            0, 0.5f * yScale, 0,
            0, 0.3f * yScale, 0,
            0.5f * xScale, 0.2f * yScale, 0,
            // 우착 하단 삼각형
            0, 0.3f * yScale, 0,
            0.5f * xScale, 0, 0,
            0.5f * xScale, 0.2f * yScale, 0
    };

    private float color[] = {0.4117647f, 0.9411764f, 0.6823529f, 0.5f};
    private float color1[] = {0.4117647f, 0.9411764f, 0.6823529f, 1.0f};
    private float highLight[] = {0.0941176f, 1.0f, 1.0f, 1.0f};

    public SimpleArrow() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(arrowCoords.length * Float.BYTES);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(arrowCoords);
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    private int positionHandle;
    private int colorHandle;

    private final int vertexCount = arrowCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * Float.BYTES;

    public void draw(float[] mvpMatrix, int colorSelector) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        switch (colorSelector) {
            case 0:
                GLES20.glUniform4fv(colorHandle, 1, color, 0);
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

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
