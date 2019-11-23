package com.bondfire.app.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bondfire.app.R;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.interfaces.IActionBarListener;

public class ActionBarView extends FrameLayout {

    private final static String Tag = "ActionBarView ";
    private final static boolean d_confgureButtons  = true;

    EditText etMessage;
    ImageButton send;

    //Settings Buttons
    ImageButton InformationSettings, GameViewSettings, SocialSettings;

    ImageView Achievements, matches;

    IActionBarListener mActionBarListener;
//    ChatListModel mChatListModel;

    ProgressBar progressBar;


    public ActionBarView(Context paramContext)
    {
        this(paramContext, null);
    }

    public ActionBarView(Context paramContext, AttributeSet paramAttributeSet)
    {
        this(paramContext, paramAttributeSet, 0);
    }

    public ActionBarView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
    }

    private MainActivity getMainActivity()
    {
        return (MainActivity)getContext();
    }

    public void AddViewBar(View mActionBarMainView){
        addView(mActionBarMainView);
    }

    public void displayViewByState(int viewState){

        getChildAt(MainActivity.Companion.getState_Cloud()).setVisibility((viewState == MainActivity.Companion.getState_Cloud())   ? View.VISIBLE:View.INVISIBLE);
        getChildAt(MainActivity.Companion.getState_Game()).setVisibility((viewState == MainActivity.Companion.getState_Game())    ? View.VISIBLE:View.INVISIBLE);
        getChildAt(MainActivity.Companion.getState_Social()).setVisibility((viewState == MainActivity.Companion.getState_Social()) ? View.VISIBLE : View.INVISIBLE);
    }

    public void showChatControls(){
        findViewById(R.id.ib_send).setVisibility(View.VISIBLE);
        findViewById(R.id.et_message).setVisibility(View.VISIBLE);
    }

    public void hideChatControls(){
        findViewById(R.id.ib_send).setVisibility(View.GONE);
        findViewById(R.id.et_message).setVisibility(View.GONE);
    }

    public void configureActionBarButtons(){
        Achievements = (ImageView)findViewById(R.id.ib_achievements);
        Achievements.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {getMainActivity().getAchievementsGPGS();
            }
        });

        matches = (ImageView)findViewById(R.id.ib_matches);
        matches.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().openMatches();
            }
        });

        GameViewSettings = (ImageButton)findViewById(R.id.ib_gamesettings);
        GameViewSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {getMainActivity().openCustomMenu(view);}
        });

        SocialSettings = (ImageButton)findViewById(R.id.ib_socialsettings);
        SocialSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TEMP", "onClick: Options Clicked");
                getMainActivity().openCustomMenu(view);}
        });

        etMessage = (EditText) findViewById(R.id.et_message);
        send = (ImageButton) findViewById(R.id.ib_send);
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = etMessage.getText().toString();
                if (!message.isEmpty() && !message.equals("")) {
                    getMainActivity().sendChat(etMessage.getText().toString());
                    etMessage.setText("");
                }
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.pb_loading);

    }

    public void showProgressBar(){
        this.progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar(){
        this.progressBar.setVisibility(View.GONE);
    }



}