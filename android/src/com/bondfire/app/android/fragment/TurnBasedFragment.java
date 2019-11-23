package com.bondfire.app.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.bondfire.app.R;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.bondfire.app.android.adapter.TurnBasedMatchesAdapter;
import com.bondfire.app.android.data.GameInformation;

import java.util.List;

/** This fragment handles the UI for when we are inside a Turn Based Game
 * We handle Quickmatch, and Find People buttons as well as display a list of people playing
 */
public class TurnBasedFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final static String Tag = TurnBasedFragment.class.getName();
    private final static boolean d_onCreateView = false;
    private final static boolean d_onCreate = false;
    private final static boolean d_onPause = false;
    private final static boolean d_onresume = false;
    private final static boolean d_newInstance = false;
    private final static boolean d_onItemClick = false;

    View rootView;
    TurnBasedMatchesAdapter mAdapter;
    ListView listView;
    GameInformation information;

    public static TurnBasedFragment newInstance(int num, GameInformation information) {
        if(d_newInstance)Log.e(Tag,"newInstance()");
        TurnBasedFragment fragment =  new TurnBasedFragment();
        fragment.information = information;
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
        if(d_onPause) Log.e(Tag,"onPause()");
        super.onPause();
    }

    @Override
    public void onResume() {
        if(d_onresume) Log.e(Tag,"onResume()");
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // return super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_turn_based, container,false);
        }else{
            Log.e(Tag,"onCreateView() rootView not Null");
        }


        mAdapter = new TurnBasedMatchesAdapter(getActivity());
        listView = (ListView)rootView.findViewById(R.id.lv_matches_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        Button findGame = (Button) rootView.findViewById(R.id.b_findGame);
        findGame.setOnClickListener(this);

        Button quickMatch = (Button) rootView.findViewById(R.id.b_quickMatch);
        quickMatch.setOnClickListener(this);

        /*Button leave = (Button) rootView.findViewById(R.id.b_onLeave);
        leave.setOnClickListener(this);*/

        ((GamePlayServiceActivity)getActivity()).injectUi(mAdapter,information.gameId);
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
            case R.id.b_findGame:
                ((GamePlayServiceActivity) getActivity()).onFindPlayersClicked(
                        information.gameId,
                        information.min,
                        information.max);
                break;
            case R.id.b_quickMatch:
                if(information!= null)
                ((GamePlayServiceActivity) getActivity()).onQuickMatch(information.gameId);
                break;
            case R.id.b_onLeave:
//                ((PlayServiceGameActivity) getActivity()).onLeaveClicked();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(d_onItemClick)Log.e(Tag,"onItemClick(), Position:" + position);
        ((GamePlayServiceActivity)getActivity()).getNetworkManager().getTurnManager().
                continueMatch(mAdapter.getItem(position).matchId);
    }
}

