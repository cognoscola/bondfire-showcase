package com.bondfire.app.android.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.badlogic.gdx.backends.android.AndroidXFragmentApplication;


public class BaseActivity extends FragmentActivity implements AndroidXFragmentApplication.Callbacks
       {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void exit() {

    }

}
