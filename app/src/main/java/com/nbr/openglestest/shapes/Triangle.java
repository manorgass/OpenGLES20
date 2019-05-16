package com.nbr.openglestest.shapes;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.nbr.openglestest.MyGLRenderer;
import com.nbr.openglestest.OpenGLES20Activity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author JungWon Kim
 * @date 월요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class Triangle {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private FloatBuffer lineVertexBuffer;

    private final int mProgram;

    // Use to access and set the view transformation
    private int vPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f,   // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f   // bottom right
    };

    static private final float upZ = 0.03f;
    static private final float downZ = -0.03f;
    static float arrowCoords[] = {   // in counterclockwise order:
            // 상단 헤드
            0, 0.4f, upZ,
            -0.3f, 0.1f, upZ,   // top
            0.3f, 0.1f, upZ, // bottom left

            // 상단 바디 1
            0.1f, 0.1f, upZ,   // bottom right
            -0.1f, 0.1f, upZ,
            -0.1f, -0.5f, upZ,

            // 상단 바디 2
            0.1f, 0.1f, upZ,   // bottom right
            -0.1f, -0.5f, upZ,
            0.1f, -0.5f, upZ,

            // 하단 헤드
            0.3f, 0.1f, downZ, // bottom left
            -0.3f, 0.1f, downZ,   // top
            0, 0.4f, downZ,

            // 하단 바디 1
            -0.1f, -0.5f, downZ,
            -0.1f, 0.1f, downZ,
            0.1f, 0.1f, downZ,   // bottom right

            // 하단 바디 2
            0.1f, -0.5f, downZ,
            -0.1f, -0.5f, downZ,
            0.1f, 0.1f, downZ,   // bottom right

            // 바디 좌측
            -0.1f, 0.1f, upZ,
            -0.1f, 0.1f, downZ,
            -0.1f, -0.5f, downZ,

            -0.1f, 0.1f, upZ,
            -0.1f, -0.5f, downZ,
            -0.1f, -0.5f, upZ,

            // 바디 하단
            -0.1f, -0.5f, upZ,
            -0.1f, -0.5f, downZ,
            0.1f, -0.5f, downZ,

            -0.1f, -0.5f, upZ,
            0.1f, -0.5f, downZ,
            0.1f, -0.5f, upZ,

            //바디 우측
            0.1f, -0.5f, downZ,
            0.1f, 0.1f, downZ,
            0.1f, 0.1f, upZ,

            0.1f, -0.5f, upZ,
            0.1f, -0.5f, downZ,
            0.1f, 0.1f, upZ,

            // 헤드 하단 좌측
            -0.3f, 0.1f, upZ,
            -0.3f, 0.1f, downZ,
            -0.1f, 0.1f, downZ,

            -0.3f, 0.1f, upZ,
            -0.1f, 0.1f, downZ,
            -0.1f, 0.1f, upZ,

            // 헤드 하단 우측
            0.1f, 0.1f, downZ,
            0.3f, 0.1f, downZ,
            0.3f, 0.1f, upZ,

            0.1f, 0.1f, upZ,
            0.1f, 0.1f, downZ,
            0.3f, 0.1f, upZ,

            // 헤드 상단 좌측
            0f, 0.4f, upZ,
            0f, 0.4f, downZ,
            -0.3f, 0.1f, downZ,

            -0.3f, 0.1f, downZ,
            -0.3f, 0.1f, upZ,
            0f, 0.4f, upZ,

            // 헤드 상단 우측
            0.3f, 0.1f, downZ,
            0f, 0.4f, downZ,
            0f, 0.4f, upZ,

            0f, 0.4f, upZ,
            0.3f, 0.1f, upZ,
            0.3f, 0.1f, downZ
    };

    static float arrowLineCoords[] = {   // in counterclockwise order:
            // 상단
            0, 0.4f, upZ,
            -0.3f, 0.1f, upZ,

            -0.3f, 0.1f, upZ,
            -0.1f, 0.1f, upZ,

            0.3f, 0.1f, upZ,
            0.1f, 0.1f, upZ,

            0.3f, 0.1f, upZ,
            0, 0.4f, upZ,

            -0.1f, 0.1f, upZ,
            -0.1f, -0.5f, upZ,

            -0.1f, -0.5f, upZ,
            0.1f, -0.5f, upZ,

            0.1f, -0.5f, upZ,
            0.1f, 0.1f, upZ,

            // 하단
            0, 0.4f, downZ,
            -0.3f, 0.1f, downZ,

            -0.3f, 0.1f, downZ,
            -0.1f, 0.1f, downZ,

            0.3f, 0.1f, downZ,
            0.1f, 0.1f, downZ,

            0.3f, 0.1f, downZ,
            0, 0.4f, downZ,

            -0.1f, 0.1f, downZ,
            -0.1f, -0.5f, downZ,

            -0.1f, -0.5f, downZ,
            0.1f, -0.5f, downZ,

            0.1f, -0.5f, downZ,
            0.1f, 0.1f, downZ,

            // 측면
            0f, 0.4f, upZ,
            0f, 0.4f, downZ,

            -0.3f, 0.1f, upZ,
            -0.3f, 0.1f, downZ,

            0.3f, 0.1f, upZ,
            0.3f, 0.1f, downZ,

            -0.11f, 0.1f, upZ,
            -0.11f, 0.1f, downZ,

            0.11f, 0.1f, upZ,
            0.11f, 0.1f, downZ,

            -0.1f, -0.5f, upZ,
            -0.1f, -0.5f, downZ,

            0.1f, -0.5f, upZ,
            0.1f, -0.5f, downZ
    };

    // Set colorMaterialPattern1 with red, green, blue and alpha (opacity) values
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};
    float colorLine[] = {0.0f, 0.0f, 0.0f, 1f};


    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(arrowCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(arrowCoords);
        vertexBuffer.position(0);

        // create a floating point buffer from the ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(arrowLineCoords.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        lineVertexBuffer = buffer.asFloatBuffer();
        lineVertexBuffer.put(arrowLineCoords);
        lineVertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // crate empty OpenGL ES Progoram
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    private int positionHandle;
    private int colorHandle;

    private final int vertexCount = arrowCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) { // pass in the calculated transformation matrix
        GLES20.glUseProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);

        drawLine(mvpMatrix);
    }

    public void drawLine(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, lineVertexBuffer);

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set colorMaterialPattern1 for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, colorLine, 0);

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(5);
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, arrowLineCoords.length / 3);
    }
}
