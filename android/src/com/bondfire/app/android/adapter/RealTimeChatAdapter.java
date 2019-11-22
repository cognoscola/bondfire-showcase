package com.bondfire.app.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bondfire.app.android.R;

/**
 * Created by alvaregd on 18/02/16.
 * Fills the Listview containing chat dialog
 */
public class RealTimeChatAdapter extends ArrayAdapter<String> {

    private static final String TAG = RealTimeParticipantAdapter.class.getName();
    public static final boolean d_getView = true;
    private Context context;

    private boolean isChatSectionVisible; // is chat view section visible?
    private ImageView notification;
    public void setNotification(ImageView notification) {this.notification = notification;}

    public RealTimeChatAdapter(Context context) {
        super(context, R.layout.entry_chat_list);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(d_getView) Log.e("ListView", "Position:" + position);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.entry_chat_list, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.b_chat_text);
        textView.setText(getItem(position));


//        textView.setText(getItem(position).getPlayer().getPlayerId());

//        TextView status = (TextView) convertView.findViewById(R.id.tv_room_status);
//        status.setText(RealTimeUtils.statusCodeToString(getItem(position).getStatus()));

        return convertView;
    }

    public void setNotificationVisible(){
        try {
            if (!isChatSectionVisible) {
                notification.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "setNotificationVisible: Notification Image view for chat messages is likely null ", e);
        }

    }

    public void isNoLongerViewingSection(){
        isChatSectionVisible = false;
    }
    public void setNotificationInvisibile(){
        isChatSectionVisible = true;
        notification.setVisibility(View.GONE);
    }
}
