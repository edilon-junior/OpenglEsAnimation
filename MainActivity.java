package com.example.openglexemple;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MyRenderer;
import com.example.openglexemple.Engine.MySurfaceView;
import com.example.openglexemple.Engine.Timer;

public class MainActivity extends Activity
{

    private static final String TAG = "MAIN_ACTIVITY";
    /** Hold a reference to our GLSurfaceView */
    private MySurfaceView mGLSurfaceView;

    private MyRenderer myRenderer;

    private Input input;
    private Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        int framePerSeconds = 60;
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new MySurfaceView(this);
        myRenderer = new MyRenderer(this);
        input = new Input();
        timer = new Timer();
        myRenderer.setInput(input);
        myRenderer.setTimer(timer);
        mGLSurfaceView.setInput(input);

        // Check if the system supports OpenGL ES 3.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs3 = configurationInfo.reqGlEsVersion >= 0x30000;

        if (supportsEs3)
        {
            // Request an OpenGL ES 3.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(3);

            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            input.setDisplayDensity(displayMetrics.density);
            input.setSensibilityFactor(16f);
            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setRenderer(myRenderer);
        }
        else
        {
            Log.i(TAG, "system not supports OpenGL ES 3.0");
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }
}