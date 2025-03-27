package com.example.openglexemple.Engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MyRenderer;

public class MySurfaceView extends GLSurfaceView {

    public static final String TAG = "MY_SURFACE_VIEW";
    private MyRenderer renderer;

    private Input input;
    private final ScaleGestureDetector scaleGestureDetector;
    // Offsets for touch events
    private float previousX = 0;
    private float previousY = 0;
    private float density;
    private float mScaleFactor = 1.f;

	public MySurfaceView(Context context)
    {
        super(context);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

	public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setRenderer(MyRenderer renderer) {
        this.renderer = renderer;
        super.setRenderer(renderer);
    }

    public void setInput(Input input){
        this.input = input;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

       // System.out.println(TAG+" touch movement: "+ getMovement()[0]+" "+getMovement()[1]);
        // Let the ScaleGestureDetector inspect all events.
        scaleGestureDetector.onTouchEvent(event);

        if (event != null)
        {

            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_DOWN){
                //System.out.println(TAG+" touch down coordinate: "+ x+" "+y);
                input.setDownCoordinates(x, y);
                input.setTouch(true);
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                float dx = x - previousX;
                float dy = y - previousY;

                float newDeltaX = dx * input.getMovementFactor();
                float newDeltaY = dy * input.getMovementFactor();

                input.setMovement(newDeltaX, newDeltaY);
            }

            if(event.getAction() == MotionEvent.ACTION_UP){
                input.setMovement(0,0);
            }

            previousX = x;
            previousY = y;

            return true;
        }
        else
        {
            return super.onTouchEvent(event);
        }
    }
}

