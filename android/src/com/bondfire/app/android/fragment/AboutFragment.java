package com.bondfire.app.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bondfire.app.R;
import com.bondfire.app.android.view.ActionBarChildView;
import com.bondfire.app.android.view.ActionBarView;

/**
 * Created by alvaregd on 22/02/16.
 */
public class AboutFragment extends Fragment {

    private static final String TAG = AboutFragment.class.getName();
    private final static boolean d_onCreateView = true;
    View rootView;

    private ChildFragmentListener listener;

    public static AboutFragment newInstance(){
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(d_onCreateView) Log.i(TAG, "onCreateView() ");
        if(rootView == null){
            rootView = inflater.inflate(R.layout.about, container, false);
        }

        ActionBarView mActionBarView = (ActionBarView)rootView.findViewById(R.id.action_bar_about);
        ActionBarChildView mActionBarProfileView =ActionBarChildView.inflateBar(getActivity(), mActionBarView);
        mActionBarView.addView(mActionBarProfileView);

        TextView title = (TextView)mActionBarProfileView.findViewById(R.id.child_action_bar_title);
        title.setText(getResources().getString(R.string.pref_entry_about));

        rootView.findViewById(R.id.b_child_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        rootView.findViewById(R.id.tv_rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "To Do",Toast.LENGTH_SHORT).show();
            }
        });

        rootView.findViewById(R.id.tv_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "To Do",Toast.LENGTH_SHORT).show();
            }
        });

        rootView.findViewById(R.id.tv_license).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                LicenseDialogFragment frag = LicenseDialogFragment.newInstance(1);
                frag.show(ft,"License Dialog");
            }
        });

        rootView.findViewById(R.id.tv_terms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getResources().getString(R.string.address_tos)));
                startActivity(i);
            }
        });

        rootView.findViewById(R.id.tv_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getResources().getString(R.string.address_policy)));
                startActivity(i);
            }
        });

        TextView credit = (TextView) rootView.findViewById(R.id.tv_credit);
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                CreditDialogFragment frag = CreditDialogFragment.newInstance(1);
                frag.show(ft,"Credit Dialog");
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.onInstanceShown(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = (ChildFragmentListener)activity;
    }
}
