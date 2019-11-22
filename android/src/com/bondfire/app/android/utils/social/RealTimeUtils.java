package com.bondfire.app.android.utils.social;

import com.bondfire.app.android.network.realtime.BondfireMessage;
import com.google.android.gms.games.multiplayer.Participant;

/**
 * Created by alvaregd on 17/02/16.
 */
public class RealTimeUtils {

    /**
     * Returns string format of Room client Statuses provided by the RealTime Service
     * These statuses are for letting the client know connection state if the room
     * For example, the clients may be invited, disconnected, joined etc...
     * @param statusCode status
     * @return name of status
     */
    public static String statusCodeToString(int statusCode) {
        switch (statusCode) {
            case Participant.STATUS_DECLINED:
                return "Declined";
            case Participant.STATUS_FINISHED:
                return "Finished";
            case Participant.STATUS_JOINED:
                return "Joined";
            case Participant.STATUS_LEFT:
                return "Left";
            case Participant.STATUS_INVITED:
                return "Invited";
            case Participant.STATUS_NOT_INVITED_YET:
                return "Left";
            case Participant.STATUS_UNRESPONSIVE:
                return "Unresponsive";
            default:
                return String.valueOf(statusCode);
        }
    }

    /** The client status lets the other clients know (via the listView) what the local
     * client is up to. For example they could be in lobby, in another game, or busy
     * @param statusCode
     * @return
     */
    public static String clientStatusToString(int statusCode) {
        switch (statusCode) {
            case BondfireMessage.STATUS_BUSY:
                return "Busy";
            case BondfireMessage.STATUS_LOBBY:
                return "Lobby";
            case BondfireMessage.STATUS_GAME:
                return "Playing:";
            default:
                return "";
        }
    }
}
