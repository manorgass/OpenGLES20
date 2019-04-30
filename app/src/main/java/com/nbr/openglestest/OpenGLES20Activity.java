package com.nbr.openglestest;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.Random;

/**
 * @author JungWon Kim
 * @date 월요일, 4월, 2019
 * @email manorgass@gmail.com
 */
public class OpenGLES20Activity extends Activity {
    private final String TAG = "OpenGLES20Activity";
    private GLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        setContentView(R.layout.activity_main);

        glView = new MyGLSurfaceView(this);

        LinearLayout openGlContainer = findViewById(R.id.opengl_container);
        openGlContainer.addView(glView);

        findViewById(R.id.switch_depth_buffer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick!");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 1; i < 5000; i += 3) {
                            renderer.setAngle((float)(i % 360));
                            glView.requestRender();
                            try {
                                Thread.sleep(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "i: " + i + " / angle : " + renderer.getAngle());
                        }
                    }
                }).start();


               /* if (v.getTag().equals("on")) {
                    v.setTag("off");
                    ((Button) v).setText("off");
                    glView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                        }
                    });

                } else {
                    v.setTag("on");
                    ((Button) v).setText("on");
                    glView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                        }
                    });
                }*/
            }
        });
    }

    private MyGLRenderer renderer;

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

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(renderer);

            // Render the view only when there is a change in the drawing data.
            // To allow the triangle to rotate automatically, this line is commented out:
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
        private float previousX;
        private float previousY;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // MotionEvent reports input details from the touch screen
            // and other input controls. In this case, you are only
            // interested in events where the touch position changed.


            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    float dx = x - previousX;
                    float dy = y - previousY;

                    // reverse direction of rotation above the mid-line
                    if (y > getHeight() / 2) {
                        dx *= -1;
                    }

                    // revers direction of rotation to  left of the mid-line
                    if (x < getWidth() / 2) {
                        dy *= -1;
                    }

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
