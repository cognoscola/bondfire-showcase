package com.bondfire.app.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bondfire.app.R;

/**
 * Created by alvaregd on 22/02/16.
 * Shows the Credits
 */
public class CreditDialogFragment extends DialogFragment {

    private static final String TAG = CreditDialogFragment.class.getName();
    private final static boolean d_onDestroy = false;
    private final static boolean d_onClick = true;
    private final static boolean d_onDestroyView = true;

    View rootView;
    /** reference to the background  */
    Bitmap bg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** Creating the view for the dialog fragment... */
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_credit_dialog, container, false);
        }

        rootView.findViewById(R.id.b_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(d_onClick) Log.i(TAG, "onClick() ");
                dismiss();
            }
        });

        return rootView;
    }

    public static CreditDialogFragment newInstance(int num){
        CreditDialogFragment f = new CreditDialogFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(d_onDestroyView) Log.i(TAG, "onDestroyView() ");
        bg        = null;
        rootView  = null;
    }
}
