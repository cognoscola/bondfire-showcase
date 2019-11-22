package com.bondfire.app.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.bondfire.app.android.R;


public class ActionBarSocialView extends FrameLayout{

    private EditText et_msg;
    public ActionBarSocialView(Context paramContext)
    {
        this(paramContext, null);
        // initialize();
    }

    public ActionBarSocialView(Context paramContext, AttributeSet paramAttributeSet)
    {
        this(paramContext, paramAttributeSet, 0);
        // initialize();
    }

    public ActionBarSocialView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        // initialize();
    }

    public static ActionBarSocialView inflateBar(Context mainContext, ViewGroup actionBarView){
        return (ActionBarSocialView) LayoutInflater.from(mainContext).inflate(R.layout.action_bar_social_no_party, actionBarView, false);
    }




}