package com.bondfire.app.android.fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.Fragment;
import androidx.core.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.bondfire.app.android.R;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.adapter.GridViewAdapter;
import com.bondfire.app.android.data.GameInformation;
import com.bondfire.app.android.data.GameInformationCollection;
import com.bondfire.app.android.interfaces.GmGameStateListener;

public class GameGridViewFragment extends Fragment  {

    private final static String Tag = "GameViewFragment ";
    private final static boolean d_onCreateView = false;
    private final static boolean d_onItemClick = false;

    /** TEMP DATA **/ //TODO REMOVE TEMP DATA
    public final static String[] GAME_TITLES = {"Swifty Glider","Dex","Line Spark"};//,"Texas Holdem"};
    public final static String[] GAME_TYPE   = {"Arcade","Arcade","Drawing"};
    public final static String[] GAME_LEADERBOARDS = {"CgkIvsqXkfIMEAIQBg","",""};
    public final static int[] GAME_MINPLAYERS = {1,1,2};
    public final static int[] GAME_MAXPPLAYER = {1,8,2};
    public final static String[] GAME_DESCRIPTION = {"Help Swifty get to the floor!",
                                                    "Help Dex find his way home!",
                                                    "There is no wrong answer. Release your creativity!"};
    public final static int[]    GAME_ICONS   = {
            R.drawable.swiftyglider_icon,
            R.drawable.linespark_icon,
            R.drawable.linespark_icon};
    public final static int[]    GAME_ID     = {0,1,2};
    public final static boolean[] USES_ADS = {true,false,false};
    public final static boolean[] USES_TBMP = {false,false,false};
    public final static boolean[] USES_LeaderBoard = {true,false,false};
    public final static boolean[] USES_DAY_CLOCK = {true, false,true};
    public final static boolean[] USES_RTMP = {true,true,false};
    public final static boolean[] USES_EVENTS ={true, false,false};

    /** Our Fragment's root view */
    View rootView;

    int width;
    int height;

    /** Grid stuff */
    private GridView GameListGrid;
    private GridViewAdapter mGameListAdapter;
    private RelativeLayout mGameView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // return super.onCreateView(inflater, container, savedInstanceState);
        if(d_onCreateView) Log.e(Tag,"onCreateView()");

        if(rootView == null){

            rootView = inflater.inflate(R.layout.fragment_game, null);
            GameListGrid = (GridView)rootView.findViewById(R.id.grid_game_list);
            mGameView = (RelativeLayout)rootView.findViewById(R.id.GameView);

            PopulateGameList();
            getString(R.string.leaderboard_glider_scores);
        }

        /** miscallanious things for the window */
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /** Populate grid view manually instead of relying on a parsers*/
    private void PopulateGameList(){

        GameInformationCollection collection = new GameInformationCollection(
                GAME_TITLES,
                GAME_LEADERBOARDS,
                GAME_ICONS,
                GAME_ID,
                GAME_MINPLAYERS,
                GAME_MAXPPLAYER
                );
        collection.setUsesAdvertisementServices(USES_ADS);
        collection.setUsesLeaderBoardServices(USES_LeaderBoard);
        collection.setUsesTurnBasedMultiPlayerServices(USES_TBMP);
        collection.setUsesRealTimeMultiplayerServices(USES_RTMP);
        collection.setUsesDayTimer(USES_DAY_CLOCK);

        mGameListAdapter = new GridViewAdapter(getActivity(), collection );
        GameListGrid.setAdapter(mGameListAdapter);
        GameListGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /** When clicking on a game, make it so that details are shown */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                ((MainActivity) getActivity()).getBackground(pixmapListener);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                GameStartDialogFragment newFragment = GameStartDialogFragment.newInstance(1, mGameStateListener);
                newFragment.show(ft, "Dialog");
                GameStartDialogFragment.injectGameBundle(mGameListAdapter.getInformation(position));
            }
        });
    }

    public static String getGameTitleById(int id) {
        return GAME_TITLES[id];
    }

    public static GameGridViewFragment newInstance(int num){
        GameGridViewFragment f = new GameGridViewFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    /** When the dialog fragment Returns PLAY, Kill yourself **/
    GmGameStateListener mGameStateListener = new GmGameStateListener() {
        @Override
        public void StartGame(GameInformation information) {

            //record the events
            if (information.gameId == 0) {
                ((GamePlayServiceActivity)getActivity()).submitEvent(
                        GamePlayServiceActivity.decryptString(getResources().getString(R.string.event_client_launched_swifty_glider))
                        ,1);
            }

            if (information.gameId == 1) {

                ((GamePlayServiceActivity)getActivity()).submitEvent(
                        GamePlayServiceActivity.decryptString(getResources().getString(R.string.event_client_launched_line_spark))
                        ,1);
            }

            ((MainActivity)getActivity()).getmSectionsPagerAdapter().replaceGameFragmentWithPlay(information);
        }
    };
}