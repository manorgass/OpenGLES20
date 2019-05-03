package com.nbr.openglestest;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

    private long animSpeed = 30;

    private Runnable highLightRunnable = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (true) {
                i++;
                renderer.highLightIndex = i % (4 + renderer.highLightArrowNum);
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
        tvHighLightArrowNum.setText("" + renderer.highLightArrowNum);
        findViewById(R.id.btn_high_light_arrow_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.highLightArrowNum++;
                tvHighLightArrowNum.setText("" + renderer.highLightArrowNum);
            }
        });
        findViewById(R.id.btn_high_light_arrow_increase).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                renderer.highLightArrowNum++;
                tvHighLightArrowNum.setText("" + renderer.highLightArrowNum);
                return true;
            }
        });

        findViewById(R.id.btn_high_light_arrow_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.highLightArrowNum != 1) {
                    renderer.highLightArrowNum--;
                    tvHighLightArrowNum.setText("" + renderer.highLightArrowNum);
                }
            }
        });

        // 에니메이션 interval 변경
        final TextView tvAnimInterval = findViewById(R.id.tv_animation_interval);
        tvAnimInterval.setText("" + animSpeed);

        findViewById(R.id.btn_animation_interval_increase).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        animSpeed += 3;
                        tvAnimInterval.setText("" + animSpeed);
                    }
                });

        findViewById(R.id.btn_animation_interval_decrease).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (animSpeed > 3) {
                            animSpeed -= 3;
                            tvAnimInterval.setText("" + animSpeed);
                        }
                    }
                });
    }

    public class MyGLSurfaceView extends GLSurfaceView {

        public MyGLSurfaceView(Context context) {
            super(context);

            // Crate an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            renderer = new MyGLRenderer();

            setZOrderMediaOverlay(true);

            // Set background transparent
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            getHolder().setFormat(PixelFormat.RGBA_8888);

            setRenderer(renderer);

            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        private final float TOUCH_SCALE_FACTOR = 180.0f / 320 / 5;
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

                    renderer.setAngle(renderer.getAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
                    requestRender();

                    break;
            }

            previousX = x;
            previousY = y;
            return true;
        }
    }


}
