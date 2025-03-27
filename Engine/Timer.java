package com.example.openglexemple.Engine;

import android.os.SystemClock;

public class Timer {

    public static final String TAG = "TIMER";

    float years = 0;
    float months = 0;
    float days = 0;
    float hours = 0;
    float minutes = 0;
    float seconds = 0;
    float startTime = 0;
    float lastLoopTime = 0;
    float deltaTime = 0;

    public Timer(){
        startTime = SystemClock.uptimeMillis();
    }

    public void update(){
        seconds = (SystemClock.uptimeMillis() - startTime)/1000; //time in seconds;
        minutes = 1/60f * seconds;
        hours = 1/60f * minutes;
        days = 1/24f * hours;
        months = 1/30f * days;
        years = 1/360f * days;
        deltaTime = seconds - lastLoopTime;
        lastLoopTime = seconds;
    }

    public float getSeconds(){return  seconds;}
    public float getDeltaTime(){
        return deltaTime;
    }

    public int[] getTime(){
        return new int[]{
                (int)seconds,
                (int)minutes,
                (int)hours,
                (int)days,
                (int)months,
                (int)years};
    }

}
