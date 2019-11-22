package com.bondfire.app.android.data;

import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

/** entry to our match listview inside the TurnBasedApi */
public class MatchEntry {

    public MatchEntry(String matchName, int matchStatus, String matchId){
        this.matchName = matchName;
        this.matchStatus = matchStatus;
        this.matchId = matchId;
    }

    public String getStatusString(){
        switch (matchStatus){
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                return "My Turn";
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                return "Their Turn";
            case TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE:
                return "Completed";
            default:
                return "Unknown";
        }
    }

    public String matchId;
    public String matchName;
    public int matchStatus;

}
