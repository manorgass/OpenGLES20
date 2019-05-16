package com.nbr.openglestest;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.nbr.openglestest.shapes.TBTDrawer;

/**
 * @author JungWon Kim
 * @date 월요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class OpenGLES20Activity extends AppCompatActivity implements ColorPickerDialogListener, View.OnClickListener {
    private final int DIALOG_LINE_COLOR = 0;
    private final int DIALOG_HIGHLIGHT_COLOR = 1;
    private final int DIALOG_PATTERN_1_COLOR = 2;
    private final int DIALOG_PATTERN_2_COLOR = 3;

    private final String TAG = "OpenGLES20Activity";

    private OpenGLES20Activity context;

    private MyGLRenderer renderer;

    private GLSurfaceView glView;

    private boolean eot = false;

    private int highlightUpdateInterval = 18;

    private int checkedNum = 1;

    private TextView tvAngle, tvAngleTotal;
    private Handler handler = new Handler();

    //private String strLineColor = "#b200ff00";
    private String strLineColor = "#ff00ff00";
    private String strHighlightColor = "#4d00ff00";
    private String strPattern1Color = "#66e9f1f0";
    private String strPattern2Color = "#6668efad";


    private Runnable highLightRunnable = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (true) {
                i++;
                renderer.highLightIndex = i % (renderer.arrowNum + renderer.highLightArrowNum);
                glView.requestRender();
                try {
                    Thread.sleep(highlightUpdateInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (eot) break;
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    View decorView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Hide the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            getSupportActionBar().hide();
        /*View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            Log.e(TAG, "system_ui_fullscreen == 0");
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            showSystemUI();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideSystemUI();
                                }
                            }, 2000);
                        } else {
                            Log.e(TAG, "else system_ui_fullscreen == 0");
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });

        glView = new MyGLSurfaceView(this);
        LinearLayout openGlContainer = findViewById(R.id.opengl_container);
        openGlContainer.addView(glView);

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

        LinearLayout tbtButtonContainer = findViewById(R.id.tbt_button_container);
        Resources res = getResources();
        TypedArray tbtButtonImage = res.obtainTypedArray(R.array.tbt_button_image);
        for (int i = 0; i < 18; i++) {
            ImageButton imageButton = new ImageButton(this);
            imageButton.setOnClickListener(this);
            imageButton.setTag(Integer.toString(i + 1));
            imageButton.setBackgroundResource(tbtButtonImage.getResourceId(i, R.drawable.tbt_1));
            tbtButtonContainer.addView(imageButton);
        }


        /** setting event 설정 **/

        // 배경 변경
        final RadioGroup radioGroup = findViewById(R.id.rg_background_1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                float eyeY = 0.25f;
                float eyeZ = 3.4f;
                float lineStride = 1.0f;
                switch (checkedId) {
                    case R.id.rb_background_black:
                        findViewById(R.id.main_container).setBackgroundColor(Color.rgb(0, 0, 0));
                        break;
                    case R.id.rb_background_white:
                        findViewById(R.id.main_container).setBackgroundColor(Color.rgb(255, 255, 255));
                        break;
                    case R.id.rb_background_road_1:
                        findViewById(R.id.main_container).setBackgroundResource(R.drawable.uturn);
                        lineStride = 0.7f;
                        eyeY = -0.15f;
                        eyeZ = 2.2f;
                        break;
                    case R.id.rb_background_road_2:
                        findViewById(R.id.main_container).setBackgroundResource(R.drawable.road2);
                        lineStride = 1.2f;
                        eyeY = 0.3f;
                        eyeZ = 3.6f;
                        break;
                    case R.id.rb_background_road_3:
                        findViewById(R.id.main_container).setBackgroundResource(R.drawable.rigthturn2);
                        eyeY = 0.45f;
                        eyeZ = 2.8f;
                        break;
                }
                renderer.updateCameraPosition(eyeY, eyeZ);
                renderer.lineStride = lineStride;
                setText(findViewById(R.id.tv_eye_y_axis_position), eyeY);
                setText(findViewById(R.id.tv_eye_z_axis_position), eyeZ);
                setText(findViewById(R.id.tv_line_stride), lineStride);
                glView.requestRender();
            }
        });

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
                setText(tvArrowNum, renderer.arrowNum);
                glView.requestRender();
            }
        });

        findViewById(R.id.btn_arrow_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.arrowNum > stride) {
                    renderer.arrowNum -= stride;
                    setText(tvArrowNum, renderer.arrowNum);
                    glView.requestRender();
                }
            }
        });

        // 에니메이션 interval 변경
        final TextView tvAnimInterval = findViewById(R.id.tv_animation_interval);
        setText(tvAnimInterval, highlightUpdateInterval);
        findViewById(R.id.btn_animation_interval_increase).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        highlightUpdateInterval += stride;
                        setText(tvAnimInterval, highlightUpdateInterval);
                    }
                });

        findViewById(R.id.btn_animation_interval_decrease).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (highlightUpdateInterval > 3) {
                            highlightUpdateInterval -= stride;
                            setText(tvAnimInterval, highlightUpdateInterval);
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
                    renderer.rotateAxisX = 1.0f;
                } else {
                    if (checkedNum == 1) {
                        cbRotationAxis_x.setChecked(true);
                    } else {
                        checkedNum--;
                        renderer.rotateAxisX = 0.0f;
                    }
                }
                glView.requestRender();
            }
        });

        final CheckBox cbRotationAxis_y = findViewById(R.id.cb_axis_y);
        cbRotationAxis_y.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedNum++;
                    renderer.rotateAxisY = 1.0f;
                } else {
                    if (checkedNum == 1) {
                        cbRotationAxis_y.setChecked(true);
                    } else {
                        checkedNum--;
                        renderer.rotateAxisY = 0.0f;
                    }
                }
                glView.requestRender();
            }
        });

        final CheckBox cbRotationAxis_z = findViewById(R.id.cb_axis_z);
        cbRotationAxis_z.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedNum++;
                    renderer.rotateAxisZ = 1.0f;
                } else {
                    if (checkedNum == 1) {
                        cbRotationAxis_z.setChecked(true);
                    } else {
                        checkedNum--;
                        renderer.rotateAxisZ = 0.0f;
                    }
                }
                glView.requestRender();
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
                glView.requestRender();
            }
        });

        findViewById(R.id.btn_rotation_start_index_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.rotationStartIndex > stride) {
                    renderer.rotationStartIndex -= stride;
                    setText(tvRotationStartIndex, renderer.rotationStartIndex);
                    glView.requestRender();
                }
            }
        });

        // 최대 회전각 설정
        final TextView tvMaxAngle = findViewById(R.id.tv_max_angle);
        setText(tvMaxAngle, renderer.maxAngle);
        findViewById(R.id.btn_max_angle_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.maxAngle += 5;
                setText(tvMaxAngle, renderer.maxAngle);
                glView.requestRender();
            }
        });

        findViewById(R.id.btn_max_angle_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderer.maxAngle > 10) {
                    renderer.maxAngle -= 5;
                    setText(tvMaxAngle, renderer.maxAngle);
                    glView.requestRender();
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
        setText(tvEyeZAxisPosition, renderer.eyeZ);
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

        // drawDots mode 설정
        final RadioGroup rgDrawMode = findViewById(R.id.rg_draw_mode);
        rgDrawMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_draw_normal:
                        renderer.materialType = TBTDrawer.MATERIAL_TYPE_ARROW;
                        glView.requestRender();
                        break;
                    case R.id.rb_draw_line_dot:
                        renderer.materialType = TBTDrawer.MATERIAL_TYPE_DOTS;
                        glView.requestRender();
                        break;
                    case R.id.rb_draw_line_cross:
                        renderer.materialType = TBTDrawer.MATERIAL_TYPE_CROSS;
                        glView.requestRender();
                        break;
                    case R.id.rb_draw_line_triangle:
                        renderer.materialType = TBTDrawer.MATERIAL_TYPE_TRIANGLE;
                        glView.requestRender();
                        break;
                }
            }
        });

        // line 두깨 설정
        final TextView tvLineWidth = findViewById(R.id.tv_line_width);
        setText(tvLineWidth, TBTDrawer.LINE_WIDTH);
        findViewById(R.id.btn_line_width_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (10 > TBTDrawer.LINE_WIDTH) {
                    TBTDrawer.LINE_WIDTH++;
                    setText(tvLineWidth, TBTDrawer.LINE_WIDTH);
                    glView.requestRender();
                }
            }
        });

        findViewById(R.id.btn_line_width_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (1 < TBTDrawer.LINE_WIDTH) {
                    TBTDrawer.LINE_WIDTH--;
                    setText(tvLineWidth, TBTDrawer.LINE_WIDTH);
                    glView.requestRender();
                }
            }
        });

        // line 간격(폭) 설정
        final TextView tvLineStride = findViewById(R.id.tv_line_stride);
        setText(tvLineStride, renderer.lineStride);
        findViewById(R.id.btn_line_stride_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.lineStride += 0.1f;
                setText(tvLineStride, renderer.lineStride);
                glView.requestRender();
            }
        });

        findViewById(R.id.btn_line_stride_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0.3f < renderer.lineStride) {
                    renderer.lineStride -= 0.1f;
                    setText(tvLineStride, renderer.lineStride);
                    glView.requestRender();
                }
            }
        });

        // 하이라이트 화살표 z 축 회전 최대 각도 설정
        final TextView tvMaxZAxisDegree = findViewById(R.id.tv_max_z_axis_degree);
        setText(tvMaxZAxisDegree, renderer.zMaxAngle);
        findViewById(R.id.btn_max_z_axis_degree_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.zMaxAngle += 3.0f;
                setText(tvMaxZAxisDegree, renderer.zMaxAngle);
            }
        });

        findViewById(R.id.btn_max_z_axis_degree_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.zMaxAngle -= 3.0f;
                setText(tvMaxZAxisDegree, renderer.zMaxAngle);
            }
        });

        // 하이라이트 화살표 z 축 1회 회전 시 각도 변경 폭 설정
        final TextView tvZAxisStride = findViewById(R.id.tv_z_spin_stride);
        setText(tvZAxisStride, renderer.zRotateStride);
        findViewById(R.id.btn_z_spin_stride_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.zRotateStride += 0.5f;
                setText(tvZAxisStride, renderer.zRotateStride);
            }
        });

        findViewById(R.id.btn_z_spin_stride_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.zRotateStride -= 0.5f;
                setText(tvZAxisStride, renderer.zRotateStride);
            }
        });

        // COLOR 설정
        findViewById(R.id.color_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_LINE_COLOR)
                        .setColor(Color.parseColor(strLineColor))
                        .setShowAlphaSlider(true)
                        .show(context);
            }
        });

        findViewById(R.id.color_highlight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_HIGHLIGHT_COLOR)
                        .setColor(Color.parseColor(strHighlightColor))
                        .setShowAlphaSlider(true)
                        .show(context);
            }
        });

        findViewById(R.id.color_pattern_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_PATTERN_1_COLOR)
                        .setColor(Color.parseColor(strPattern1Color))
                        .setShowAlphaSlider(true)
                        .show(context);
            }
        });

        findViewById(R.id.color_pattern_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_PATTERN_2_COLOR)
                        .setColor(Color.parseColor(strPattern2Color))
                        .setShowAlphaSlider(true)
                        .show(context);
            }
        });
    }

    @Override
    protected void onStop() {
        eot = true;
        super.onStop();
    }

    /**
     * Color Picker Selected Listener
     * <p>
     * 색상 선택 다이어로그에서 색상을 선택 시 해당 call back 이 호출됨.
     *
     * @param dialogId 호출 시 설정한 다이어로그 아이디. 어느 용도로 호출했는지 구분하기 위해 사용.
     * @param color    선택한 색상. int는 4바이트 이므로 0xAARRGGBB 형태로 값이 들어있다.
     */
    @Override
    public void onColorSelected(int dialogId, int color) {
        float a, r, g, b;
        String strColor = Integer.toHexString(color); // aarrggbb 형태의 string으로 변환
        /* 단순하고 무식하게 처리! a r g b 값을 split 후, OpenGL GLSL 가 이해할 수 있도록 float 형태로 변환  */
        a = (float) Integer.parseInt("" + strColor.charAt(0) + strColor.charAt(1), 16) / 255.0f;
        r = (float) Integer.parseInt("" + strColor.charAt(2) + strColor.charAt(3), 16) / 255.0f;
        g = (float) Integer.parseInt("" + strColor.charAt(4) + strColor.charAt(5), 16) / 255.0f;
        b = (float) Integer.parseInt("" + strColor.charAt(6) + strColor.charAt(7), 16) / 255.0f;
        switch (dialogId) {
            case DIALOG_LINE_COLOR:
                strLineColor = "#" + strColor; // 앞에 #을 안붙여주면 색상 문자열로 인식 못함.
                findViewById(R.id.color_line).setBackgroundColor(Color.parseColor(strLineColor));
                renderer.setLineColor(r, g, b, a);
                break;
            case DIALOG_HIGHLIGHT_COLOR:
                strHighlightColor = "#" + strColor;
                findViewById(R.id.color_highlight).setBackgroundColor(Color.parseColor(strHighlightColor));
                renderer.setHighlightColor(r, g, b, a);
                break;
            case DIALOG_PATTERN_1_COLOR:
                strPattern1Color = "#" + strColor;
                findViewById(R.id.color_pattern_1).setBackgroundColor(Color.parseColor(strPattern1Color));
                renderer.setPattern1Color(r, g, b, a);
                break;
            case DIALOG_PATTERN_2_COLOR:
                strPattern2Color = "#" + strColor;
                findViewById(R.id.color_pattern_2).setBackgroundColor(Color.parseColor(strPattern2Color));
                renderer.setPattern2Color(r, g, b, a);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, v.getTag().toString());
        updateMaxZAngle(0);

        switch (Integer.parseInt(v.getTag().toString())) {
            case 1: // 직진
                renderer.TBT_TYPE = renderer.TBT_GO_STRAIGHT;
                updateAngle(0.0f);
                break;
            case 2: // 1시 방향
                renderer.TBT_TYPE = renderer.TBT_DIRECTION_1;

                break;
            case 3: // 우회전
                renderer.TBT_TYPE = renderer.TBT_TURN_RIGHT;
                updateMaxZAngle(30.0f);
                updateZRotateStride(2.0f);
                updateAngle(-5.0f);
                updateMaxAngle(90);
                break;
            case 4: // 4시 방향
                renderer.TBT_TYPE = renderer.TBT_DIRECTION_4;
                updateAngle(-12.0f);
                updateMaxAngle(165);
                break;
            case 5: // 7시 방향
                renderer.TBT_TYPE = renderer.TBT_DIRECTION_7;
                updateAngle(12.0f);
                updateMaxAngle(165);
                break;
            case 6: // 좌회전
                renderer.TBT_TYPE = renderer.TBT_TURN_LEFT;
                updateMaxZAngle(30.0f);
                updateZRotateStride(2.0f);
                updateAngle(5.0f);
                updateMaxAngle(90);
                break;
            case 7: // 10시 방향
                renderer.TBT_TYPE = renderer.TBT_DIRECTION_10;
                break;
            case 8: // 우측 방향
                renderer.TBT_TYPE = renderer.TBT_RIGHT_SIDE;
                updateAngle(-8.0f);
                updateMaxAngle(30);
                updateMaxZAngle(12);
                updateZRotateStride(1.0f);
                break;
            case 9: // 좌측 방향
                renderer.TBT_TYPE = renderer.TBT_LEFT_SIDE;
                updateAngle(8.0f);
                updateMaxAngle(30);
                updateMaxZAngle(12);
                updateZRotateStride(1.0f);
                break;
            case 10: // 유턴
                renderer.TBT_TYPE = renderer.TBT_U_TURN;
                updateMaxAngle(185);
                updateAngle(10.0f);
                break;
            case 11: // 로터리 1시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_1;
                break;
            case 12: // 로터리 3시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_3;
                break;
            case 13: // 로터리 4시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_4;
                break;
            case 14: // 로터리 6시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_6;
                break;
            case 15: // 로터리 7시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_7;
                break;
            case 16: // 로터리 9시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_9;
                break;
            case 17: // 로터리 10시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_10;
                break;
            case 18: // 로터리 12시 방향
                renderer.TBT_TYPE = renderer.TBT_ROTRAY_12;
                break;
        }
        glView.requestRender();
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
                    updateAngle(angle);
                    glView.requestRender();
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

    private void updateMaxAngle(int maxAngle) {
        setText(findViewById(R.id.tv_max_angle), maxAngle);
        renderer.maxAngle = maxAngle;
    }

    private void updateAngle(float angle) {
        tvAngle.setText("Angle : " + Float.toString(angle));
        tvAngleTotal.setText("Angle Total : " + Float.toString(angle * (renderer.arrowNum - renderer.rotationStartIndex)));
        renderer.setAngle(angle);
    }

    private void updateMaxZAngle(float zMaxAngle) {
        setText(findViewById(R.id.tv_max_z_axis_degree), zMaxAngle);
        renderer.zMaxAngle = zMaxAngle;
    }

    private void updateZRotateStride(float zRotateStride) {
        setText(findViewById(R.id.tv_z_spin_stride), zRotateStride);
        renderer.zRotateStride = zRotateStride;
    }
}
