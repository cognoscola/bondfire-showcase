package com.bondfire.app.android.fragment;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bondfire.app.R;

public class UselessFragment extends Fragment {

    View rootView;

    public static UselessFragment newInstance(){
        return new UselessFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null ){
            rootView = inflater.inflate(R.layout.fragment_main, container , false);
        }
        return rootView;
    }
}
