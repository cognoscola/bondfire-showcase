package com.bondfire.app.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bondfire.app.R;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.fragment.GameGridViewFragment;
import com.bondfire.app.android.network.realtime.BondfireMessage;
import com.bondfire.app.android.network.realtime.BondfireParticipant;
import com.bondfire.app.android.network.realtime.RealTimeManager;
import com.bondfire.app.android.utils.social.RealTimeUtils;
import com.google.android.gms.games.multiplayer.Participant;

/**
 * Created by alvaregd on 17/02/16.
 * Manages the items in the participant listview.
 */
public class RealTimeParticipantAdapter extends ArrayAdapter<BondfireParticipant> {

    private static final String TAG = RealTimeParticipantAdapter.class.getName();
    public static final boolean d_getView = true;

    private Context context;

    private boolean roomConnected = false;
    private boolean isParticipantSectionVisible;
    private ImageView notification;

    public void setNotification(ImageView notification) {this.notification = notification;}
    public void setRoomConnected(boolean roomConnected) {this.roomConnected = roomConnected;}

    public RealTimeParticipantAdapter(Context context) {
        super(context, R.layout.entry_participants);
        this.context = context;
        roomConnected = false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.entry_participants, parent, false);
        }
        String text;

        TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
        final BondfireParticipant bfParticipant = getItem(position);
        final Participant participant = bfParticipant.getParticipant();
        try {
            text = participant.getDisplayName();
            textView.setText(text);

        } catch (NullPointerException e) {
            text = participant.getDisplayName().split(" ")[0] + "?";
            textView.setText(text);
        }

        TextView status = (TextView) convertView.findViewById(R.id.tv_room_status);
        status.setText(RealTimeUtils.statusCodeToString(participant.getStatus()));

        if (roomConnected) {
            if (participant.getStatus() == Participant.STATUS_JOINED) {
                //After a bunch of error checks, see if they are playing a game
                //If yes, say they game they are playing.
                if (bfParticipant.getClientStatus() == BondfireMessage.STATUS_GAME) {
                    text = RealTimeUtils.clientStatusToString(bfParticipant.getClientStatus())
                            + GameGridViewFragment.getGameTitleById(Integer.valueOf(bfParticipant.getGameId()));
                    status.setText(text);
                }
                //Just give the client status
                else{
                    text = RealTimeUtils.clientStatusToString(bfParticipant.getClientStatus());
                    status.setText(text);
                }
            } else {

                status.setText(RealTimeUtils.statusCodeToString(bfParticipant.getClientStatus()));
            }
        }

        if (bfParticipant.getRoundScore() > 0) {
            ((TextView)convertView.findViewById(R.id.tv_participant_score))
                    .setText(String.valueOf(bfParticipant.getRoundScore()));
        }

        //Create a on item click listener
        convertView.findViewById(R.id.b_overflow_participant).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchor) {

                PopupMenu popMenu = new PopupMenu(context, anchor);
                popMenu.inflate(R.menu.participant_menu);
                popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_make_host:
                                ((MainActivity) context)
                                        .getNetworkManager()
                                        .getRealTimeManager()
                                        .changeHost(participant.getParticipantId());
                                break;
                            case R.id.action_invite_to_game:
                                ((MainActivity) context)
                                        .getNetworkManager()
                                        .getRealTimeManager()
                                        .sendGameInvitation(participant);
                                break;
                            case R.id.action_join_game:
                                try {
                                    ((MainActivity)context).getNetworkManager().getRealTimeManager().setSkipToMultiplayer(true);
                                    ((MainActivity) context).configureGameManager(Integer.parseInt(bfParticipant.getGameId()));

                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "onMenuItemClick: Tried to start game on the Join Game Command", e);
                                }
                                break;
                        }
                        return false;
                    }
                });

                //also only show the change host option  if the room is connected
                //if i am a GAME leader, AND this participant entry is also in the same game
                if (roomConnected && ((MainActivity) context).getNetworkManager().getRealTimeManager().isClientGameHost()
                        && RealTimeManager.isParticipantInSameGameAsMe(participant.getParticipantId())
                        && bfParticipant.isReadyToReceiveGameData()
                        ) {
                    popMenu.getMenu().findItem(R.id.action_make_host).setVisible(true);
                }else{
                    popMenu.getMenu().findItem(R.id.action_make_host).setVisible(false);
                }

                //only show the invite button if the participant is NOT us AND we're inside the game && we're Game connection Ready,
                //AND the participant is NOT game connection Ready
                if (((MainActivity) context).getMGameManager().isLoaded() && !bfParticipant.isReadyToReceiveGameData() &&
                        !RealTimeManager.isOurClient(participant.getParticipantId())
                        ) {
                    popMenu.getMenu().findItem(R.id.action_invite_to_game).setVisible(true);
                }else{
                    popMenu.getMenu().findItem(R.id.action_invite_to_game).setVisible(false);
                }

                Log.i(TAG, "onClick() Is not our Client:" + !RealTimeManager.isOurClient(participant.getParticipantId()));
                Log.i(TAG, "onClick() Is not in same game: "+ !RealTimeManager.isParticipantInSameGameAsMe(participant.getParticipantId()));
                Log.i(TAG, "onClick() Is Ready to receive game data: " + bfParticipant.isReadyToReceiveGameData());

                //only hide the join game button if we are in the same game as the participant and we are also game ready
                if (!RealTimeManager.isOurClient(participant.getParticipantId())
                        && !RealTimeManager.isParticipantInSameGameAsMe(participant.getParticipantId())
                        && bfParticipant.isReadyToReceiveGameData()
                        && bfParticipant.getClientStatus() == BondfireMessage.STATUS_GAME ) {
                    popMenu.getMenu().findItem(R.id.action_join_game).setVisible(true);
                }else{
                    popMenu.getMenu().findItem(R.id.action_join_game).setVisible(false);
                }

                popMenu.show();
            }
        });

        return convertView;
    }

    //show the notification icon only we aren't viewing the participant section
    public void setNotificationVisible(){
        try {
            if (!isParticipantSectionVisible) {
                notification.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "setNotificationVisible: Notification Image view for chat messages is likely null ", e);
        }
    }

    public void isNoLongerViewingSection(){
        isParticipantSectionVisible = false;
    }

    // hide the notification icon
    public void setNotificationInvisibile(){
        isParticipantSectionVisible = true;
        notification.setVisibility(View.GONE);
    }

}
