package com.bondfire.app.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;


public class LockableViewPager extends ViewPager  {

    private final static String Tag = LockableViewPager.class.getName();
    private final static boolean d_debug = false;

    Context context;

    int width;
    int height;

    private boolean isGamePaused = true;
    private boolean isTouchingDownAtTheRightSpots = false;
    private boolean letGoRecently = false;

    public LockableViewPager(Context context) {
        super(context);
        init(context);
    }

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        this.context = context;

        Display display =((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //TRUE wil make the touch event go to the ViewPager
        //False will pass it down

        if(this.getCurrentItem() == 1  && !isGamePaused){

            //IF we started touching down on the edges, proceed normally //
            if(ev.getAction() == MotionEvent.ACTION_DOWN ){

                if(ev.getX() < width * 0.05 || ev.getX() > width * 0.95){
                    isTouchingDownAtTheRightSpots = true;
                }else{
                    return false;
                }
                return true;
//                System.out.println("Touching at edge" );
            }

            if(ev.getAction() == MotionEvent.ACTION_UP){
                if(d_debug)System.out.println("Let go" );
                isTouchingDownAtTheRightSpots = false;
//                letGoRecently = true;
                return true;
            }

            if(isTouchingDownAtTheRightSpots || letGoRecently){
                if(d_debug) System.out.println("returned super Inside condition" );
                return super.onTouchEvent(ev);
            } else{
                return false;
            }
        }else {
            if(d_debug) System.out.println("Returned Super" );
            return super.onTouchEvent(ev);
        }
    }


    public void settled(){
        this.letGoRecently = false;
        isTouchingDownAtTheRightSpots = false;
    }

    public void setGamePaused(boolean pause){
        this.isGamePaused = pause;
    }



}
