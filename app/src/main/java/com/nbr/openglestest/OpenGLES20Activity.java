package com.nbr.openglestest;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

/**
 * @author JungWon Kim
 * @date 월요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class OpenGLES20Activity extends Activity {
    private final String TAG = "OpenGLES20Activity";

    private Handler handler;

    private MyGLRenderer renderer;

    private GLSurfaceView glView;

    private boolean eot = false;

    private int animSpeed = 30;

    private int checkedNum = 1;

    private TextView tvAngle, tvAngleTotal;

    private Runnable highLightRunnable = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (true) {
                i++;
                renderer.highLightIndex = i % (renderer.arrowNum + renderer.highLightArrowNum);
                glView.requestRender();
                try {
                    Thread.sleep(animSpeed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (eot) break;
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glView = new MyGLSurfaceView(this);
        LinearLayout openGlContainer = findViewById(R.id.opengl_container);
        openGlContainer.addView(glView);

        handler = new Handler();

        ImageButton btnShowLog = findViewById(R.id.btn_show_log);
        btnShowLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals("visible")) {
                    v.setTag("invisible");
                    findViewById(R.id.setting_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.setting_container).setZ(1);
                    ((ImageButton) v).setImageResource(R.drawable.baseline_visibility_off_black_36);
                } else {
                    v.setTag("visible");
                    findViewById(R.id.setting_container).setVisibility(View.INVISIBLE);
                    ((ImageButton) v).setImageResource(R.drawable.baseline_visibility_black_36);
                }
            }
        });

        /** setting event 설정 **/

        // 하이라이트 에니메이션 스위치
        Switch swHighLight = findViewById(R.id.sw_highlight);
        swHighLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    renderer.highLightIndex = 0;
                    eot = false;
                    new Thread(highLightRunnable).start();
                } else {
                    renderer.highLightIndex = -1;
                    eot = true;
                    glView.requestRender();
                }
            }
        });

        // 하이라이트 갯수 변경
        final TextView tvHighLightArrowNum = findViewById(R.id.tv_high_light_arrow_num);
        setText(tvHighLightArrowNum, renderer.highLightArrowNum);
        findViewById(R.id.btn_high_light_arrow_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.highLightArrowNum++;
                setText(tvHighLightArrowNum, renderer.highLightArrowNum);
            }
        });

        findViewById(R.id.btn_high_light_arrow_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.highLightArrowNum != 1) {
                    renderer.highLightArrowNum--;
                    setText(tvHighLightArrowNum, renderer.highLightArrowNum);
                }
            }
        });

        // 화살표 갯수 변경
        final TextView tvArrowNum = findViewById(R.id.tv_arrow_num);
        final int stride = 3;
        setText(tvArrowNum, renderer.arrowNum);
        findViewById(R.id.btn_arrow_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.arrowNum += stride;
                glView.requestRender();
                setText(tvArrowNum, renderer.arrowNum);
            }
        });

        findViewById(R.id.btn_arrow_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.arrowNum > stride) {
                    renderer.arrowNum -= stride;
                    glView.requestRender();
                    setText(tvArrowNum, renderer.arrowNum);
                }
            }
        });

        // 에니메이션 interval 변경
        final TextView tvAnimInterval = findViewById(R.id.tv_animation_interval);
        setText(tvAnimInterval, animSpeed);
        findViewById(R.id.btn_animation_interval_increase).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        animSpeed += stride;
                        setText(tvAnimInterval, animSpeed);
                    }
                });

        findViewById(R.id.btn_animation_interval_decrease).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (animSpeed > 3) {
                            animSpeed -= stride;
                            setText(tvAnimInterval, animSpeed);
                        }
                    }
                });

        // 회전 축 설정
        final CheckBox cbRotationAxis_x = findViewById(R.id.cb_axis_x);
        cbRotationAxis_x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedNum++;
                    renderer.arrowRotationAxis_x = 1.0f;
                } else {
                    if (checkedNum == 1) {
                        cbRotationAxis_x.setChecked(true);
                    } else {
                        checkedNum--;
                        renderer.arrowRotationAxis_x = 0.0f;
                    }
                }
            }
        });

        final CheckBox cbRotationAxis_y = findViewById(R.id.cb_axis_y);
        cbRotationAxis_y.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedNum++;
                    renderer.arrowRotationAxis_y = 1.0f;
                } else {
                    if (checkedNum == 1) {
                        cbRotationAxis_y.setChecked(true);
                    } else {
                        checkedNum--;
                        renderer.arrowRotationAxis_y = 0.0f;
                    }
                }
            }
        });

        final CheckBox cbRotationAxis_z = findViewById(R.id.cb_axis_z);
        cbRotationAxis_z.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedNum++;
                    renderer.arrowRotationAxis_z = 1.0f;
                } else {
                    if (checkedNum == 1) {
                        cbRotationAxis_z.setChecked(true);
                    } else {
                        checkedNum--;
                        renderer.arrowRotationAxis_z = 0.0f;
                    }
                }
            }
        });

        // 각도 표시
        tvAngle = findViewById(R.id.tv_angle);
        tvAngleTotal = findViewById(R.id.tv_angle_total);

        // 회전 시작 지점 변경
        final TextView tvRotationStartIndex = findViewById(R.id.tv_rotation_start_index);
        setText(tvRotationStartIndex, renderer.rotationStartIndex);
        findViewById(R.id.btn_rotation_start_index_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.rotationStartIndex += stride;
                setText(tvRotationStartIndex, renderer.rotationStartIndex);
            }
        });

        findViewById(R.id.btn_rotation_start_index_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.rotationStartIndex > stride) {
                    renderer.rotationStartIndex -= stride;
                    setText(tvRotationStartIndex, renderer.rotationStartIndex);
                }
            }
        });

        // 최대 회전각 설정
        final TextView tvMaxAngle = findViewById(R.id.tv_max_angle);
        setText(tvMaxAngle, renderer.maxAngle);
        findViewById(R.id.btn_max_angle_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.maxAngle += 10;
                setText(tvMaxAngle, renderer.maxAngle);
            }
        });

        findViewById(R.id.btn_max_angle_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.maxAngle > 10) {
                    renderer.maxAngle -= 10;
                    setText(tvMaxAngle, renderer.maxAngle);
                }
            }
        });

        // View Matrix y축 포지션 설정
        final TextView tvEyeYAxisPosition = findViewById(R.id.tv_eye_y_axis_position);
        setText(tvEyeYAxisPosition, renderer.eyeY);
        findViewById(R.id.btn_eye_y_axis_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.eyeY += 0.05f;
                renderer.updateCameraPosition();
                glView.requestRender();
                setText(tvEyeYAxisPosition, renderer.eyeY);

            }
        });

        findViewById(R.id.btn_eye_y_axis_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.eyeY -= 0.05f;
                renderer.updateCameraPosition();
                glView.requestRender();
                setText(tvEyeYAxisPosition, renderer.eyeY);
            }
        });

        // View Matrix z축 포지션 설정
        final TextView tvEyeZAxisPosition = findViewById(R.id.tv_eye_z_axis_position);
        setText(tvEyeYAxisPosition, renderer.eyeZ);
        findViewById(R.id.btn_eye_z_axis_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.eyeZ += 0.2f;
                renderer.updateCameraPosition();
                glView.requestRender();
                setText(tvEyeZAxisPosition, renderer.eyeZ);

            }
        });

        findViewById(R.id.btn_eye_z_axis_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.eyeZ -= 0.2f;
                renderer.updateCameraPosition();
                glView.requestRender();
                setText(tvEyeZAxisPosition, renderer.eyeZ);
            }
        });
    }

    public class MyGLSurfaceView extends GLSurfaceView {

        public MyGLSurfaceView(Context context) {
            super(context);

            // Crate an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            renderer = new MyGLRenderer();

            setZOrderOnTop(true);

            // Set background transparent
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            getHolder().setFormat(PixelFormat.RGBA_8888);

            setRenderer(renderer);

            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        private final float TOUCH_SCALE_FACTOR = 180.0f / 320 / 10;
        private float previousX;
        private float previousY;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    float dx = x - previousX;
                    float dy = y - previousY;

                    // reverse direction of rotation above the mid-line
                    if (y > getHeight() / 2) dx *= -1;

                    // revers direction of rotation to  left of the mid-line
                    if (x < getWidth() / 2) dy *= -1;

                    float angle = renderer.getAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR);
                    tvAngle.setText("Angle : " + Float.toString(angle));
                    tvAngleTotal.setText("Angle Total : " + Float.toString(angle * (renderer.arrowNum - renderer.rotationStartIndex)));
                    renderer.setAngle(angle);
                    requestRender();

                    break;
            }

            previousX = x;
            previousY = y;
            return true;
        }
    }

    private void setText(View v, int num) {
        String text = String.format("%02d", num);
        ((TextView) v).setText(text);
    }

    private void setText(View v, float num) {
        String text = String.format("%.2f", num);
        ((TextView) v).setText(text);
    }
}
