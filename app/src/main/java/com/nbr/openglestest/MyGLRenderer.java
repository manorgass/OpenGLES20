package com.nbr.openglestest;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.nbr.openglestest.shapes.TBTDrawer;
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
    // TBT info
    final int TBT_GO_STRAIGHT = 1;
    final int TBT_DIRECTION_1 = 2;
    final int TBT_TURN_RIGHT = 4;
    final int TBT_DIRECTION_4 = 5;
    final int TBT_DIRECTION_7 = 7;
    final int TBT_TURN_LEFT = 9;
    final int TBT_DIRECTION_10 = 10;
    final int TBT_RIGHT_SIDE = 20;
    final int TBT_LEFT_SIDE = 21;
    final int TBT_U_TURN = 35;
    final int TBT_ROTRAY_1 = 38;
    final int TBT_ROTRAY_3 = 40;
    final int TBT_ROTRAY_4 = 41;
    final int TBT_ROTRAY_6 = 43;
    final int TBT_ROTRAY_7 = 44;
    final int TBT_ROTRAY_9 = 46;
    final int TBT_ROTRAY_10 = 47;
    final int TBT_ROTRAY_12 = 49;


    // 선과 관련된 shape
    private TBTDrawer tbtDrawer;

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
    float rotateAxisX = 0.0f;
    float rotateAxisY = 1.0f;
    float rotateAxisZ = 0.0f;
    // 회전 시작 인덱스
    int rotationStartIndex = 37;
    // 최대 회전각. 해당 각도 이상으로 꺾이지 않음.
    int maxAngle = 190;
    // camera의 위치. z축은 값이 커질수록 객체와 멀어짐.
    float eyeY = 0.25f;
    float eyeZ = 3.4f;
    // draw flag. 해당 값을 통해 무엇을 그릴지 결정
    int materialType = TBTDrawer.MATERIAL_TYPE_TRIANGLE;
    // highlight arrow 의 z-axis 회전 관련 변수
    float zMaxAngle = 0;           // 최대 회전각
    float zRotateStride = 3.0f;      // 회전 폭
    // activity의 touch event 를 통해 유저가 원하는 회전각을 저장
    private float angle;
    // 라인 간격
    public float lineStride = 1.0f;
    // TBT 신호
    public int TBT_TYPE = TBT_GO_STRAIGHT;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame colorMaterialPattern1
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
        tbtDrawer = new TBTDrawer();
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG, "onDrawFrame");

        // Redraw background colorMaterialPattern1
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);

        // 좌/우 line의 좌표를 저장할 float 배열
        float[] rightLineCoords = new float[arrowNum * 3];
        float[] leftLineCoords = new float[arrowNum * 3];

        // 정점 버퍼 인덱스
        int arrayIndex = 0;
        // 지속적인 rotation과 translate 시 좌표 계산을 위한 기준점을 저장
        float baseX = 0;
        float baseZ = 0;
        // 사용자 터치 조작에 의한 y축 총 회전각을 저장
        float totalRotateDegree = 0;
        // highlight arrow의 총 회전각을 저장
        float zRotateDegree = 0;
        // Object 간 z축 간격
        float drawStride = -0.3f;
        // 정점 당 x, y, z 3가지 값을 가지므로 stride는 3
        int vertexStride = 3;
        float[] materialCoords = null;

        // init highlight arrow coordinates
        initHighlightArrow();

        // init material coordinates
        switch (materialType) {
            // 화살표 형태
            case TBTDrawer.MATERIAL_TYPE_ARROW:
                materialCoords = new float[6 * vertexStride];
                // vertex 1
                materialCoords[0] = 0.0f;
                materialCoords[1] = 0.0f;
                materialCoords[2] = -0.3f;
                // vertex 2
                materialCoords[3] = lineStride - 0.1f;
                materialCoords[4] = 0.0f;
                materialCoords[5] = 0.0f;
                // vertex 3
                materialCoords[6] = lineStride - 0.1f;
                materialCoords[7] = 0.0f;
                materialCoords[8] = -0.2f;
                // vertex 4
                materialCoords[9] = 0.0f;
                materialCoords[10] = 0.0f;
                materialCoords[11] = -0.5f;
                // vertex 5
                materialCoords[12] = -(lineStride - 0.1f);
                materialCoords[13] = 0.0f;
                materialCoords[14] = -0.2f;
                // vertex 6
                materialCoords[15] = -(lineStride - 0.1f);
                materialCoords[16] = 0.0f;
                materialCoords[17] = 0.0f;
                break;
            case TBTDrawer.MATERIAL_TYPE_DOTS:
                int dotsCount = ((int) (lineStride * 10) - 1) * 2 + 1;
                materialCoords = new float[dotsCount * 3];
                for (int i = ((int) (lineStride * 10) - 1) * -1; i < (int) (lineStride * 10); i++) {
                    materialCoords[arrayIndex] = (float) i / 10.0f;
                    materialCoords[arrayIndex + 1] = 0.0f;
                    materialCoords[arrayIndex + 2] = 0.0f;
                    arrayIndex += vertexStride;
                }
                tbtDrawer.setMaterialCoords(materialCoords);
                break;

            case TBTDrawer.MATERIAL_TYPE_CROSS:
                // 십자가 하나는 정점 4개로 그림
                int vertexPerCross = 4;
                float crossLength = 0.025f;
                int crossCount = ((int) (lineStride * 10) - 1) * 2 + 1;
                materialCoords = new float[crossCount * vertexPerCross * vertexStride];
                for (int i = ((int) (lineStride * 10) - 1) * -1; i < (int) (lineStride * 10); i++) {
                    float centerX = (float) i / 10.0f;
                    // cross vertex 1
                    materialCoords[arrayIndex] = centerX + crossLength;
                    materialCoords[arrayIndex + 1] = 0.0f;
                    materialCoords[arrayIndex + 2] = 0.0f;
                    // cross vertex 2
                    materialCoords[arrayIndex + 3] = centerX - crossLength;
                    materialCoords[arrayIndex + 4] = 0.0f;
                    materialCoords[arrayIndex + 5] = 0.0f;
                    // cross vertex 3
                    materialCoords[arrayIndex + 6] = centerX;
                    materialCoords[arrayIndex + 7] = 0.0f;
                    materialCoords[arrayIndex + 8] = crossLength;
                    // cross vertex 4
                    materialCoords[arrayIndex + 9] = centerX;
                    materialCoords[arrayIndex + 10] = 0.0f;
                    materialCoords[arrayIndex + 11] = -crossLength;

                    arrayIndex += vertexStride * vertexPerCross;
                }
                break;

            case TBTDrawer.MATERIAL_TYPE_TRIANGLE:
                float triangleScale = 0.025f;
                int vertexPerTriangle = 6;
                int triangleCount = ((int) (lineStride * 10) - 1) * 2 + 1;
                materialCoords = new float[triangleCount * vertexPerTriangle * vertexStride];
                for (int i = ((int) (lineStride * 10) - 1) * -1; i < (int) (lineStride * 10); i++) {
                    // vertex 1
                    materialCoords[arrayIndex] = (float) i / 10.0f + triangleScale;
                    materialCoords[arrayIndex + 1] = 0.0f;
                    materialCoords[arrayIndex + 2] = triangleScale;
                    // vertex 2
                    materialCoords[arrayIndex + 3] = (float) i / 10.0f - triangleScale;
                    materialCoords[arrayIndex + 4] = 0.0f;
                    materialCoords[arrayIndex + 5] = triangleScale;
                    // vertex 3
                    materialCoords[arrayIndex + 6] = (float) i / 10.0f - triangleScale;
                    materialCoords[arrayIndex + 7] = 0.0f;
                    materialCoords[arrayIndex + 8] = triangleScale;
                    // vertex 4
                    materialCoords[arrayIndex + 9] = (float) i / 10.0f;
                    materialCoords[arrayIndex + 10] = 0.0f;
                    materialCoords[arrayIndex + 11] = -triangleScale;
                    // vertex 5
                    materialCoords[arrayIndex + 12] = (float) i / 10.0f;
                    materialCoords[arrayIndex + 13] = 0.0f;
                    materialCoords[arrayIndex + 14] = -triangleScale;
                    // vertex 6
                    materialCoords[arrayIndex + 15] = (float) i / 10.0f + triangleScale;
                    materialCoords[arrayIndex + 16] = 0.0f;
                    materialCoords[arrayIndex + 17] = triangleScale;

                    arrayIndex += vertexStride * vertexPerTriangle;
                }

                break;
        }


        tbtDrawer.setMaterialCoords(materialCoords);

        arrayIndex = 0;

        // 설정된 arrow 갯수만큼 그리기
        for (int i = 0; i < arrowNum; i++) {
            // 총 회전된 각도가 최대 각을 넘지 않고, 현재 그리는 순번이 회전 시작 index보다 큰 경우에만 회전
            if (Math.abs(totalRotateDegree) < maxAngle && i > rotationStartIndex) {
                // 다음 회전이 최대각을 넘는 경우 최대각으로 만들어줌.
                if (Math.abs(totalRotateDegree + angle) > maxAngle) {
                    if (angle > 0)  // 각이 양수인 경우 max - currentDegree 만큼 회전
                        Matrix.rotateM(modelMatrix, 0, maxAngle - totalRotateDegree, rotateAxisX, rotateAxisY, rotateAxisZ);
                    else  // 각이 음수인 경우 -(max + currentDegree) 만큼 회전
                        Matrix.rotateM(modelMatrix, 0, -(maxAngle + totalRotateDegree), rotateAxisX, rotateAxisY, rotateAxisZ);

                    totalRotateDegree = maxAngle * (angle > 0 ? 1 : -1);
                } else {
                    Matrix.rotateM(modelMatrix, 0, angle, rotateAxisX, rotateAxisY, rotateAxisZ);
                    // 총 회전각도 업데이트
                    totalRotateDegree += angle;
                }
            }

            // line coords 계산
            if (arrayIndex == rightLineCoords.length) break;
            // x axis position
            rightLineCoords[arrayIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(90 + totalRotateDegree)));
            leftLineCoords[arrayIndex] = baseX + (lineStride * (float) Math.sin(Math.toRadians(-90 + totalRotateDegree)));
            // y axis position 값은 0으로 고정이며, 초기값이 0이므로 굳이 값을 대입하지 않음
            // z axis position
            rightLineCoords[arrayIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(90 + totalRotateDegree)));
            leftLineCoords[arrayIndex + 2] = baseZ + (lineStride * (float) Math.cos(Math.toRadians(-90 + totalRotateDegree)));

            // 다음 line 좌표 계산 시 중점이 될 base 좌표를 갱신
            baseX += drawStride * (float) Math.sin(Math.toRadians(totalRotateDegree));
            baseZ += drawStride * (float) Math.cos(Math.toRadians(totalRotateDegree));

            // x, y, z 축의 값을 업데이트 했으므로 다음 x좌표 값 갱신을 위해 좌표 index를 vertex stirde인 3만큼 증가
            arrayIndex += 3;

            // 하이라이트 화살표 메트릭스 초기화.
            // model matrix를 복사하는 이유는 해당 model matrix와 동일한 위치에서 z축 회전만 시킬 것이기 때문이다.
            // 계속해서 z축으로 직진하는 컨셉이므로 해당 축을 건드리면 전체 3축이 변형되어 밑으로 혹은 위로 점점 올라간다.
            System.arraycopy(modelMatrix, 0, highlightArrowModelMatrix, 0, 16);

            // 최대 회전 각보다 작은 경우 stride 만큼 z축으로 회전. 사용자가 지정한 앵글의 부호에 따라 + / - 를 정해준다.
            if (i > rotationStartIndex) {
                Matrix.rotateM(highlightArrowModelMatrix, 0, angle > 0 ? zRotateDegree : -zRotateDegree, 0.0f, 0.0f, 1.0f);
                if (zRotateDegree < zMaxAngle) zRotateDegree += zRotateStride;
            }

            // mvp matrix 계산
            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

            // 하이라이트 분기처리
            if (highLightIndex >= i && highLightIndex <= i + highLightArrowNum) {
                tbtDrawer.drawMaterial(mvpMatrix, 0, materialType);
                // 하이라이트 위치에서만 하이라이트 오브젝트를 그려준다.
                Matrix.multiplyMM(highlightArrowMvpMatrix, 0, viewMatrix, 0, highlightArrowModelMatrix, 0);
                Matrix.multiplyMM(highlightArrowMvpMatrix, 0, projectionMatrix, 0, highlightArrowMvpMatrix, 0);
                tbtDrawer.drawHighLightArrow(highlightArrowMvpMatrix);
            } else {
                // 번갈아가면서 road 색상을 변경해준다.
                tbtDrawer.drawMaterial(mvpMatrix, i % 2 == 0 ? 1 : 2, materialType);
            }
            // draw stride 만큼 z축으로 이동
            Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, drawStride);
        }
        // Draw left line
        tbtDrawer.setLineVertexBuffer(leftLineCoords);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        tbtDrawer.drawLine(mvpMatrix);
        // Draw right line
        tbtDrawer.setLineVertexBuffer(rightLineCoords);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.5f, 0.0f);
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        tbtDrawer.drawLine(mvpMatrix);
    }


    public void initHighlightArrow() {
        float yAxis = 0.15f; //해당 값만큼 위에 떠있음.
        float[] highlightArrowCoords = new float[]{
                0.0f, yAxis, -0.7f,
                (lineStride - 0.3f), yAxis, 0.0f,
                (lineStride - 0.3f), yAxis, -0.2f,
                0.0f, yAxis, -1.0f,
                -(lineStride - 0.3f), yAxis, -0.2f,
                -(lineStride - 0.3f), yAxis, 0.0f
        };
        tbtDrawer.setHighLightArrowVertexBuffer(highlightArrowCoords);
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


    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        Log.d(TAG, "setAngle: " + angle);
        this.angle = angle;

    }

    /**
     * 카메라 포지션 변경.
     * setLookAtM은 카메라 위치 뿐만 아닌 각도, 포커싱 포인트 등의 값을 가지고 있지만,
     * eye 값만 변경해주기 때문에 해당 메서드는 포지션(만) 변경!
     */
    public void updateCameraPosition(float eyeY, float eyeZ) {
        this.eyeZ = eyeZ;
        this.eyeY = eyeY;
        Matrix.setLookAtM(viewMatrix, 0, 0.0f, eyeY, eyeZ, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public void updateCameraPosition() {
        Matrix.setLookAtM(viewMatrix, 0, 0.0f, this.eyeY, this.eyeZ, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public void setLineColor(float r, float g, float b, float a) {
        tbtDrawer.colorLine[0] = r;
        tbtDrawer.colorLine[1] = g;
        tbtDrawer.colorLine[2] = b;
        tbtDrawer.colorLine[3] = a;
    }

    public void setHighlightColor(float r, float g, float b, float a) {
        tbtDrawer.colorHighlight[0] = r;
        tbtDrawer.colorHighlight[1] = g;
        tbtDrawer.colorHighlight[2] = b;
        tbtDrawer.colorHighlight[3] = a;
    }

    public void setPattern1Color(float r, float g, float b, float a) {
        tbtDrawer.colorMaterialPattern1[0] = r;
        tbtDrawer.colorMaterialPattern1[1] = g;
        tbtDrawer.colorMaterialPattern1[2] = b;
        tbtDrawer.colorMaterialPattern1[3] = a;
    }

    public void setPattern2Color(float r, float g, float b, float a) {
        tbtDrawer.colorMaterialPattern2[0] = r;
        tbtDrawer.colorMaterialPattern2[1] = g;
        tbtDrawer.colorMaterialPattern2[2] = b;
        tbtDrawer.colorMaterialPattern2[3] = a;
    }


}
