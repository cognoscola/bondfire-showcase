package com.bondfire.app.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.bondfire.app.android.R;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.bondfire.app.android.adapter.TurnBasedParticipantsAdapter;
import com.bondfire.app.android.data.GameInformation;

import java.util.List;

public class TurnBasedRoomFragment extends BaseFragment implements View.OnClickListener {

    private final static String Tag = TurnBasedRoomFragment.class.getName();
    private final static boolean d_onCreateView = false;
    private final static boolean d_onCreate = false;
    private final static boolean d_onPause = false;
    private final static boolean d_onresume = false;
    private final static boolean d_newInstance = false;

    View rootView;
    TurnBasedParticipantsAdapter mAdapter;
    ListView listView;
    GameInformation information;

    public static TurnBasedRoomFragment newInstance(int num) {
        if (d_newInstance) Log.e(Tag, "newInstance()");
        TurnBasedRoomFragment fragment = new TurnBasedRoomFragment();

        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (d_onCreate) Log.e(Tag, "OnCreate()");
    }

    @Override
    public void onPause() {
        if (d_onPause) Log.e(Tag, "onPause()");
        super.onPause();
    }

    @Override
    public void onResume() {
        if (d_onresume) Log.e(Tag, "onResume()");
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // return super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_turn_based_room, container, false);
        } else {
            Log.e(Tag, "onCreateView() rootView not Null");
        }
        mAdapter = new TurnBasedParticipantsAdapter(getActivity());
        listView = (ListView) rootView.findViewById(R.id.lv_room_participants);
        listView.setAdapter(mAdapter);

        Button findGame = (Button) rootView.findViewById(R.id.b_switch_matches);
        findGame.setOnClickListener(this);

        Button leave = (Button) rootView.findViewById(R.id.b_leave_room);
        leave.setOnClickListener(this);

        ((GamePlayServiceActivity) getActivity()).injectUi(mAdapter);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    protected List<Object> getModules() {
        return null;// return Arrays.<Object>asList(new ChatListModule());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.b_switch_matches:
                ((MainActivity)getActivity()).getmSectionsPagerAdapter().showTurnBasedMatches();
                break;

            case R.id.b_leave_room:
                ((GamePlayServiceActivity) getActivity()).onLeaveClicked();
                break;
        }
    }
}

