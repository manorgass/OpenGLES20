package com.nbr.openglestest;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.nbr.openglestest.shapes.LineDot;
import com.nbr.openglestest.shapes.SimpleArrow;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author JungWon Kim
 * @date 월요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final String TAG = this.getClass().getSimpleName();
    // draw mode
    static final int DRAW_NORMAL = 0;           // 일반 화살표
    static final int DRAW_LINE_DOT = 1;         // 선과 점
    static final int DRAW_LINE_CROSS = 2;       // 선과 십자가
    static final int DRAW_LINE_TRIANGLE = 3;    // 선과 삼각형

    // 일반적인 화살표 모양의 shape
    private SimpleArrow simpleArrow;
    // 선과 관련된 shape
    private LineDot lineDot;

    // projection matrix
    private float[] projectionMatrix = new float[16];
    // view matrix
    private float[] viewMatrix = new float[16];
    // model - view - projection matrix
    private float[] mvpMatrix = new float[16];
    // model matrix
    private float[] modelMatrix = new float[16];

    // highlight arrow's model matrix
    private float[] highlightArrowModelMatrix = new float[16];
    // highlight arrow's model - view - projection matrix
    private float[] highlightArrowMvpMatrix = new float[16];


    // Highlight 갯수를 설정
    int highLightArrowNum = 3;
    int highLightIndex = -1;

    // 화면에 나타낼 객체의 갯수를 저장
    int arrowNum = 80;

    // rotation setting 관련 변수. 사용자 터치에 의해 회전할 축을 결정
    float arrowRotationAxis_x = 0.0f;
    float arrowRotationAxis_y = 1.0f;
    float arrowRotationAxis_z = 0.0f;
    // 회전 시작 인덱스
    int rotationStartIndex = 10;
    // 최대 회전각. 해당 각도 이상으로 꺾이지 않음.
    int mainMaxAngle = 190;
    // camera의 위치. z축은 값이 커질수록 객체와 멀어짐.
    float eyeY = 0.25f;
    float eyeZ = 3.4f;
    // draw flag. 해당 값을 통해 무엇을 그릴지 결정
    int drawMode = 0;
    // highlight arrow 의 z-axis 회전 관련 변수

    float zMaxAngle = 0;           // 최대 회전각
    float zRotateStride = 3.0f;      // 회전 폭
    // activity의 touch event 를 통해 유저가 원하는 회전각을 저장
    private float yRotateStride;

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

        // init shapes
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

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);

        // 좌/우 line의 좌표를 저장할 float 배열
        float[] rightLineCoords;
        float[] leftLineCoords;

        // 정점 버퍼 인덱스
        int coordsBufferIndex;
        // 지속적인 rotation과 translate 시 좌표 계산을 위한 기준점을 저장
        float baseX, baseZ;
        // 사용자 터치 조작에 의한 y축 총 회전각을 저장
        float yRotateDegree;
        // highlight arrow의 총 회전각을 저장
        float zRotateDegree;
        // Object 간 z축 간격
        float drawStride;

        // drawDots arrow
        switch (drawMode) {
            // 기본 형태의 사각형 화살표 그리기
            case DRAW_NORMAL:
                yRotateDegree = 0;
                drawStride = -0.3f;
                for (int i = 0; i < arrowNum; i++) {
                    Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, drawStride);
                    if (Math.abs(yRotateDegree) < mainMaxAngle && rotationStartIndex < i) {
                        // 다음 회전이 최대각을 넘는 경우 최대각으로 만들어줌.
                        if (Math.abs(yRotateDegree + yRotateStride) > mainMaxAngle) {
                            if (yRotateStride > 0)  // 각이 양수인 경우 max - currentDegree 만큼 회전
                                Matrix.rotateM(modelMatrix, 0, mainMaxAngle - yRotateDegree, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                            else  // 각이 음수인 경우 -(max + currentDegree) 만큼 회전
                                Matrix.rotateM(modelMatrix, 0, -(mainMaxAngle + yRotateDegree), arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                        } else {
                            Matrix.rotateM(modelMatrix, 0, yRotateStride, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                        }
                        yRotateDegree += yRotateStride;
                    }
                    // Model - View - Projection Matrix 계산
                    Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                    // 하이라이트 처리
                    if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum)
                        simpleArrow.draw(mvpMatrix, 0);
                    else
                        simpleArrow.draw(mvpMatrix, i % 2 == 0 ? 1 : 2);
                }
                break;

            // 선과 점으로 방향신호를 표현
            case DRAW_LINE_DOT:
                rightLineCoords = new float[arrowNum * 3];
                leftLineCoords = new float[arrowNum * 3];
                coordsBufferIndex = 0;
                baseX = 0;
                baseZ = 0;
                yRotateDegree = 0;
                drawStride = -0.3f;

                zRotateDegree = 0;

                // line stride 를 기준으로 화면에 그릴 점들의 좌표 계산
                int dotCount = ((int) (lineStride * 10) - 1) * 2 + 1;
                float[] dotsCoords = new float[dotCount * 3];
                for (int i = ((int) (lineStride * 10) - 1) * -1; i < (int) (lineStride * 10); i++) {
                    dotsCoords[coordsBufferIndex] = (float) i / 10.0f;
                    dotsCoords[coordsBufferIndex + 1] = 0.0f;
                    dotsCoords[coordsBufferIndex + 2] = 0.0f;
                    coordsBufferIndex += 3;
                }
                lineDot.setDotsVertexBuffer(dotsCoords);

                coordsBufferIndex = 0;

                for (int i = 0; i < arrowNum; i++) {
                    // 총 회전된 각도가 최대 각을 넘지 않고, 현재 그리는 순번이 회전 시작 index보다 큰 경우에만 회전
                    if (Math.abs(yRotateDegree) < mainMaxAngle && i > rotationStartIndex) {
                        // 다음 회전이 최대각을 넘는 경우 최대각으로 만들어줌.
                        if (Math.abs(yRotateDegree + yRotateStride) > mainMaxAngle) {
                            if (yRotateStride > 0)  // 각이 양수인 경우 max - currentDegree 만큼 회전
                                Matrix.rotateM(modelMatrix, 0, mainMaxAngle - yRotateDegree, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                            else  // 각이 음수인 경우 -(max + currentDegree) 만큼 회전
                                Matrix.rotateM(modelMatrix, 0, -(mainMaxAngle + yRotateDegree), arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);

                            yRotateDegree = mainMaxAngle * (yRotateStride > 0 ? 1 : -1);
                        } else {
                            Matrix.rotateM(modelMatrix, 0, yRotateStride, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                            // 총 회전각도 업데이트
                            yRotateDegree += yRotateStride;
                        }
                    }

                    // line coords 계산
                    if (coordsBufferIndex == rightLineCoords.length) break;
                    // x axis position
                    rightLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + yRotateDegree)));
                    leftLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + yRotateDegree)));
                    // y axis position 값은 0으로 고정이며, 초기값이 0이므로 굳이 값을 대입하지 않음
                    // z axis position
                    rightLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + yRotateDegree)));
                    leftLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + yRotateDegree)));

                    // 다음 line 좌표 계산 시 중점이 될 base 좌표를 갱신
                    baseX += drawStride * (float) Math.sin(Math.toRadians(yRotateDegree));
                    baseZ += drawStride * (float) Math.cos(Math.toRadians(yRotateDegree));

                    // x, y, z 축의 값을 업데이트 했으므로 다음 x좌표 값 갱신을 위해 좌표 index를 vertex stirde인 3만큼 증가
                    coordsBufferIndex += 3;

                    // 하이라이트 화살표 메트릭스 초기화.
                    // model matrix를 복사하는 이유는 해당 model matrix와 동일한 위치에서 z축 회전만 시킬 것이기 때문이다.
                    // 계속해서 z축으로 직진하는 컨셉이므로 해당 축을 건드리면 전체 3축이 변형되어 밑으로 혹은 위로 점점 올라간다.
                    System.arraycopy(modelMatrix, 0, highlightArrowModelMatrix, 0, 16);

                    // 최대 회전 각보다 작은 경우 stride 만큼 z축으로 회전. 사용자가 지정한 앵글의 부호에 따라 + / - 를 정해준다.
                    if (i > rotationStartIndex) {
                        Matrix.rotateM(highlightArrowModelMatrix, 0, yRotateStride > 0 ? zRotateDegree : -zRotateDegree, 0.0f, 0.0f, 1.0f);
                        if (zRotateDegree < zMaxAngle) zRotateDegree += zRotateStride;
                    }

                    // mvp matrix 계산
                    Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                    // 하이라이트 분기처리
                    if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                        lineDot.drawDots(mvpMatrix, 0);
                        // 하이라이트 위치에서만 하이라이트 오브젝트를 그려준다.
                        Matrix.multiplyMM(highlightArrowMvpMatrix, 0, viewMatrix, 0, highlightArrowModelMatrix, 0);
                        Matrix.multiplyMM(highlightArrowMvpMatrix, 0, projectionMatrix, 0, highlightArrowMvpMatrix, 0);
                        lineDot.drawHighLightArrow(highlightArrowMvpMatrix);
                    } else {
                        // 번갈아가면서 road 색상을 변경해준다.
                        lineDot.drawDots(mvpMatrix, i % 2 == 0 ? 1 : 2);
                    }
                    // draw stride 만큼 z축으로 이동
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
                yRotateDegree = 0;
                drawStride = -0.3f;

                zRotateDegree = 0;

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
                    if (i > rotationStartIndex && ((i - rotationStartIndex) * yRotateStride < mainMaxAngle && (i - rotationStartIndex) * yRotateStride > -mainMaxAngle)) {
                        Matrix.rotateM(modelMatrix, 0, yRotateStride, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);
                        // 회전 각도 계산
                        yRotateDegree += yRotateStride;
                        yRotateDegree %= 360;
                    }

                    // 하이라이트 화살표 메트릭스 초기화
                    System.arraycopy(modelMatrix, 0, highlightArrowModelMatrix, 0, 16);
                    // 최대 회전 각보다 작은 경우 stride 만큼 z축으로 회전. 사용자가 지정한 앵글의 부호에 따라 + / - 를 정해준다.
                    if (i > rotationStartIndex) {
                        if (zRotateDegree < zMaxAngle) {
                            if (yRotateStride > 0)
                                Matrix.rotateM(highlightArrowModelMatrix, 0, zRotateDegree, 0.0f, 0.0f, 1.0f);
                            else
                                Matrix.rotateM(highlightArrowModelMatrix, 0, -zRotateDegree, 0.0f, 0.0f, 1.0f);
                            zRotateDegree += zRotateStride;
                        } else {
                            if (yRotateStride > 0)
                                Matrix.rotateM(highlightArrowModelMatrix, 0, zRotateDegree, 0.0f, 0.0f, 1.0f);
                            else
                                Matrix.rotateM(highlightArrowModelMatrix, 0, -zRotateDegree, 0.0f, 0.0f, 1.0f);
                        }
                    }

                    // line coords 계산
                    if (coordsBufferIndex == rightLineCoords.length) break;
                    rightLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + yRotateDegree)));
                    leftLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + yRotateDegree)));
                    rightLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + yRotateDegree)));
                    leftLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + yRotateDegree)));


                    baseX += drawStride * (float) Math.sin(Math.toRadians(yRotateDegree));
                    baseZ += drawStride * (float) Math.cos(Math.toRadians(yRotateDegree));

                    // index 증가
                    coordsBufferIndex += 3;


                    Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
                    if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                        lineDot.drawCross(mvpMatrix, 0);
                        Matrix.multiplyMM(highlightArrowMvpMatrix, 0, viewMatrix, 0, highlightArrowModelMatrix, 0);
                        Matrix.multiplyMM(highlightArrowMvpMatrix, 0, projectionMatrix, 0, highlightArrowMvpMatrix, 0);
                        lineDot.drawHighLightArrow(highlightArrowMvpMatrix);
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
                yRotateDegree = 0;
                drawStride = -0.3f;

                zRotateDegree = 0;

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

                    if (i > rotationStartIndex && ((i - rotationStartIndex) * yRotateStride < mainMaxAngle && (i - rotationStartIndex) * yRotateStride > -mainMaxAngle)) {
                        Matrix.rotateM(modelMatrix, 0, yRotateStride, arrowRotationAxis_x, arrowRotationAxis_y, arrowRotationAxis_z);

                        // 회전 각도 계산
                        yRotateDegree += yRotateStride;
                        yRotateDegree %= 360;
                    }

                    // 하이라이트 화살표 메트릭스 초기화
                    System.arraycopy(modelMatrix, 0, highlightArrowModelMatrix, 0, 16);
                    // 최대 회전 각보다 작은 경우 stride 만큼 z축으로 회전. 사용자가 지정한 앵글의 부호에 따라 + / - 를 정해준다.
                    if (i > rotationStartIndex) {
                        if (zRotateDegree < zMaxAngle) {
                            if (yRotateStride > 0)
                                Matrix.rotateM(highlightArrowModelMatrix, 0, zRotateDegree, 0.0f, 0.0f, 1.0f);
                            else
                                Matrix.rotateM(highlightArrowModelMatrix, 0, -zRotateDegree, 0.0f, 0.0f, 1.0f);
                            zRotateDegree += zRotateStride;
                        } else {
                            if (yRotateStride > 0)
                                Matrix.rotateM(highlightArrowModelMatrix, 0, zRotateDegree, 0.0f, 0.0f, 1.0f);
                            else
                                Matrix.rotateM(highlightArrowModelMatrix, 0, -zRotateDegree, 0.0f, 0.0f, 1.0f);
                        }
                    }


                    // line coords 계산
                    if (coordsBufferIndex == rightLineCoords.length) break;
                    rightLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + yRotateDegree)));
                    leftLineCoords[coordsBufferIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + yRotateDegree)));
                    rightLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + yRotateDegree)));
                    leftLineCoords[coordsBufferIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + yRotateDegree)));


                    baseX += drawStride * (float) Math.sin(Math.toRadians(yRotateDegree));
                    baseZ += drawStride * (float) Math.cos(Math.toRadians(yRotateDegree));

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


    public float getyRotateStride() {
        return yRotateStride;
    }

    public void setyRotateStride(float yRotateStride) {
        Log.d(TAG, "setyRotateStride: " + yRotateStride);
        this.yRotateStride = yRotateStride;

    }

    /**
     * 카메라 포지션 변경.
     * setLookAtM은 카메라 위치 뿐만 아닌 각도, 포커싱 포인트 등의 값을 가지고 있지만,
     * eye 값만 변경해주기 때문에 해당 메서드는 포지션(만) 변경!
     */
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

    public void setLineColor(float r, float g, float b, float a) {
        lineDot.colorLine[0] = r;
        lineDot.colorLine[1] = g;
        lineDot.colorLine[2] = b;
        lineDot.colorLine[3] = a;
    }

    public void setHighlightColor(float r, float g, float b, float a) {
        lineDot.colorHighlightArrow[0] = r;
        lineDot.colorHighlightArrow[1] = g;
        lineDot.colorHighlightArrow[2] = b;
        lineDot.colorHighlightArrow[3] = a;
    }

    public void setPattern1Color(float r, float g, float b, float a) {
        lineDot.color[0] = r;
        lineDot.color[1] = g;
        lineDot.color[2] = b;
        lineDot.color[3] = a;
    }

    public void setPattern2Color(float r, float g, float b, float a) {
        lineDot.color1[0] = r;
        lineDot.color1[1] = g;
        lineDot.color1[2] = b;
        lineDot.color1[3] = a;
    }


}
