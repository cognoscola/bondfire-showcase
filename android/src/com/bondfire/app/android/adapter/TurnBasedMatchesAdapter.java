package com.bondfire.app.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bondfire.app.R;
import com.bondfire.app.android.data.MatchEntry;

import java.util.HashMap;

/** Populates the list of matches in any particular game */
public class TurnBasedMatchesAdapter extends ArrayAdapter<MatchEntry> {

    private final static String Tag = TurnBasedParticipantsAdapter.class.getName();
    private final static boolean d_onFriendListReceived = false;
    private final static boolean d_getView = false;

    Context context;

    HashMap<View, String> view_to_username;

    public TurnBasedMatchesAdapter(Context context) {
        super(context, R.layout.entry_participants);
        this.context = context;
        view_to_username = new HashMap<View, String>();
    }


    @Override
    public void addAll(MatchEntry... items) {
        super.addAll(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO MAKE THIS MORE EFFICIENT

        if (d_getView) Log.e("ListView", "Position:" + position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.entry_matches, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
        textView.setText(getItem(position).matchName);

        TextView status = (TextView) convertView.findViewById(R.id.tv_match_status);
        status.setText(getItem(position).getStatusString());


        return convertView;
    }

}