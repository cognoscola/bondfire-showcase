package com.bondfire.app.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bondfire.app.R;

public class ActionBarProfileView extends FrameLayout {

    public ActionBarProfileView(Context paramContext)
    {
        this(paramContext, null);
    }

    public ActionBarProfileView(Context paramContext, AttributeSet paramAttributeSet)
    {
        this(paramContext, paramAttributeSet, 0);
    }

    public ActionBarProfileView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        //d.a(this);
    }

    public static ActionBarProfileView inflateBar(Context mainContext, ViewGroup actionBarView){

        return (ActionBarProfileView) LayoutInflater.from(mainContext).inflate(R.layout.action_bar_profile, actionBarView, false);
    }

}
