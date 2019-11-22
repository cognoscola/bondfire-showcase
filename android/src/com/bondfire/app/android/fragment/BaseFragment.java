package com.bondfire.app.android.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import java.util.List;

public abstract class BaseFragment extends Fragment {

    private boolean mShowingChild;
    private boolean mNonLockingChild;

//    private ObjectGraph activityGraph;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            return;
        }

//        if(getModules() != null){
//            activityGraph = ((App) getActivity().getApplication()).createScopedGraph(getModules().toArray());
//            activityGraph.inject(this);

    }

    @Override public void onDestroy() {
        super.onDestroy();
//        activityGraph = null;
    }

    protected abstract List<Object> getModules();

    public  boolean isShowingChild() {
        return mShowingChild;
    }
    public void setShowingChild(boolean showingChild) {
        mShowingChild = showingChild;
    }

    public boolean isNonLockingChild() {return mNonLockingChild;}
    public void setNonLockingChild(boolean NonlockingChild) {this.mNonLockingChild = NonlockingChild;}



}