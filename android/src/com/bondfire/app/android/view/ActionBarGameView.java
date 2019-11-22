package com.bondfire.app.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bondfire.app.android.R;

public class ActionBarGameView extends FrameLayout{

    public ActionBarGameView(Context paramContext)
    {
        this(paramContext, null);
        // initialize();
    }

    public ActionBarGameView(Context paramContext, AttributeSet paramAttributeSet)
    {
        this(paramContext, paramAttributeSet, 0);
        // initialize();
    }

    public ActionBarGameView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        // initialize();
    }

    public static ActionBarGameView inflateBar(Context mainContext, ViewGroup actionBarView){
        return (ActionBarGameView) LayoutInflater.from(mainContext).inflate(R.layout.action_bar_game, actionBarView, false);
    }
}
