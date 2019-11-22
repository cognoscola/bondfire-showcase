package com.bondfire.app.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bondfire.app.android.R;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.bondfire.app.android.data.GameInformation;
import com.bondfire.app.android.interfaces.GmGameStateListener;

public class GameStartDialogFragment extends DialogFragment {

    private final static String Tag = "GameDescriptionFragment";
    private final static boolean d_onDestroy = false;
    private final static boolean d_onCreateView = false;
    private final static boolean d_lifecycle = false;

    View rootView;
    /** reference to the background  */

    Bitmap bg;

    int ID;

    GmGameStateListener mListener;

    public static GameInformation info;

    public static void injectGameBundle(GameInformation information){
        info = information;
    }

    private void injectListener(GmGameStateListener listener){
        this.mListener = listener;
    }

    private void injecBackground(Bitmap bg){
        this.bg = bg;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(androidx.core.app.DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** Creating the view for the dialog fragment... */
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_game_details, container, false);
        }

        /** configure the other UI */
        ((TextView)rootView.findViewById(R.id.tv_game_title)).setText(info.title);
        ((TextView)rootView.findViewById(R.id.player_count)).setText(info.getPlayerCountString());

        rootView.findViewById(R.id.b_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.StartGame(info);
                dismiss();
            }
        });
        rootView.findViewById(R.id.b_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        rootView.findViewById(R.id.leader_board).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GamePlayServiceActivity)getActivity()).getLeaderboardGPGS(info.leaderBoardId);
            }
        });
        return rootView;
    }


    public static GameStartDialogFragment newInstance(int num, GmGameStateListener listener){
        GameStartDialogFragment f = new GameStartDialogFragment();
        f.injectListener(listener);
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    public static GameStartDialogFragment newInstance(int num,Bitmap bg,GmGameStateListener listener){
        GameStartDialogFragment f = new GameStartDialogFragment();
        f.injecBackground(bg);
        f.injectListener(listener);
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(d_onDestroy)
            Log.e(Tag,"OnDestroy");
        bg        = null;
        rootView  = null;
        mListener = null;
    }
}