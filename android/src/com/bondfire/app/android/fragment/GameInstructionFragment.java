package com.bondfire.app.android.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bondfire.app.android.R;

import java.util.List;

/**
 * Created by alvaregd on 17/09/15.
 * Possibly show the leader board here as well
 */
public class GameInstructionFragment extends BaseFragment{

    private final static String Tag = "GameInstructionFrg ";

    private final static boolean d_onCreateView = true;
    private final static boolean d_onCreate = true;
    private final static boolean d_onMessagedReceived = false;
    private final static boolean d_interfaceUpdater_general = false;
    private final static boolean d_onPause = false;
    private final static boolean d_onresume = false;

    //Views
    View rootView;
    TextView title;
    TextView content;

    public static GameInstructionFragment newInstance(){
        return new GameInstructionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(d_onCreate) Log.e(Tag, "OnCreate()");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(d_onCreateView)Log.e(Tag,"onCreateView()");
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_instruction, null);

            title = ((TextView)rootView.findViewById(R.id.tvTitle));
            content = ((TextView)rootView.findViewById(R.id.tvContent));

            title.setText("");
            content.setText("");
        }


        return rootView;
    }


    @Override
    protected List<Object> getModules() {
        return null;// return Arrays.<Object>asList(new ChatListModule());
    }

    public void setInformation(String title,String content){
        this.title.setText(title);
        this.content.setText(content);
    }

}