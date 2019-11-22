package com.bondfire.app.android.background;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class BackgroundView extends FrameLayout {

    private BackgroundGradientView bggradient;

    public BackgroundView(Context paramContext)
    {
        this(paramContext, null);
    }

    public BackgroundView(Context paramContext, AttributeSet paramAttributeSet)
    {
        this(paramContext, paramAttributeSet, 0);
    }

    public BackgroundView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);

        this.bggradient = new BackgroundGradientView(paramContext, paramAttributeSet);
        addView(bggradient);
    }

    public void setBackgroundConnectionState(BackgroundGradientView.ConnectionState state){
        bggradient.SetConnectionState(state);
    }

    public Bitmap getBackgroundBitmap(){
        return bggradient.getBackgroundBitmap();
    }

    public void PauseAnimation(){
        bggradient.stopAnimation();
    }

    public void ResumeAniation(){
        bggradient.startAnimation();
    }



}