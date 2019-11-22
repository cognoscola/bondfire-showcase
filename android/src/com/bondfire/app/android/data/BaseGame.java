package com.bondfire.app.android.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public abstract class BaseGame  {

    protected String title;
    protected int layoutId;
    protected Context context;
    protected ViewGroup parent;

    public abstract void Load(LayoutInflater inflate, ViewGroup parent);
    public abstract void onResume();
    public abstract void onPause();
    public abstract void onExit();
    public abstract void refreshFrame();


}
