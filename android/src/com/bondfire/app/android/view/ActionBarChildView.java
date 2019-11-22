package com.bondfire.app.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bondfire.app.android.R;

public class ActionBarChildView extends FrameLayout {

    public ActionBarChildView(Context paramContext)
    {
        this(paramContext, null);
        // initialize();
    }

    public ActionBarChildView(Context paramContext, AttributeSet paramAttributeSet)
    {
        this(paramContext, paramAttributeSet, 0);
        // initialize();
    }

    public ActionBarChildView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        // initialize();
    }

    public static ActionBarChildView inflateBar(Context mainContext, ViewGroup actionBarView){
        return (ActionBarChildView) LayoutInflater.from(mainContext).inflate(R.layout.action_bar_child_layout, actionBarView, false);
    }
}
