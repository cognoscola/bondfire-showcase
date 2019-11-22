package com.bondfire.app.android.managers;

import android.util.Log;

import com.bondfire.app.android.adapter.TurnBasedMatchesAdapter;
import com.bondfire.app.android.data.MatchEntry;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by alvaregd on 21/10/15.
 * Manages all of the TBMP Matches
 */
public class MatchManager {
    private final static String Tag = MatchManager.class.getName();
    private final static boolean d_LoadMatches = true;
    private final static boolean d_updateAdapter = true;

    GoogleApiClient mGoogleApiClient;                     //the google client
    TurnBasedMultiplayer.LoadMatchesResult matchesResult; //keep a collection of matches
    HashMap<String, TurnBasedMatch> matches;              //a place to store our matches
    TurnBasedMatchesAdapter matchesAdapter;               //the adpater that populates the matches listview


    public void setGoogleClient(GoogleApiClient client){
        this.mGoogleApiClient = client;
    }
    public void setMatchesAdpater(TurnBasedMatchesAdapter adapter, int gameId){
        this.matchesAdapter = adapter;
        matchesAdapter.clear();
        if(matches.size() >0){
            updateMatchAdapter(gameId);
        }
        matchesAdapter.notifyDataSetChanged();
    }


    public MatchManager() {
        matches = new HashMap<>();
    }

    public void LoadMatches(final int gameId){

        if(d_LoadMatches) Log.e(Tag, "LoadMatches()");

        try{
            if(matchesResult != null){
                matchesResult.release();
            }
            int[] statuses = new int[] {
                    TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN,
                    TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN,
                    TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE
            };
            Games.TurnBasedMultiplayer.loadMatchesByStatus( mGoogleApiClient, statuses )
                    .setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.LoadMatchesResult result) {
                            if (result.getStatus().getStatusCode() != GamesStatusCodes.STATUS_OK) {
                                Log.e(Tag, "we don't have a useful error but we have an integer that we have to look up in the documentation: " + result.getStatus().getStatusCode());
                            } else {

                                matchesResult = result;
                                // so, we just wanted a List of matches but Google would never make it that simple. We'll create the
                                // matches List and fill it ourselves
                                matches.clear();

                                // what do we get from Google instead? a LoadMatchesResponse (result.getMatches returns it) of course!
                                // and what is inside a LoadMatchesResponse? three TurnBasedMatchBuffer objects! to get those matches we
                                // asked for we have to loop through each of the three TurnBasedMatchBuffers and extract the matches
                                // one-by-one.

                                if (result.getMatches().hasData()) {

                                    for (int i = 0; i < result.getMatches().getCompletedMatches().getCount(); i++) {
                                        TurnBasedMatch m = result.getMatches().getCompletedMatches().get(i);
                                        matches.put(m.getMatchId(), m);
                                    }

                                    for (int i = 0; i < result.getMatches().getMyTurnMatches().getCount(); i++) {
                                        TurnBasedMatch m = result.getMatches().getMyTurnMatches().get(i);
                                        matches.put(m.getMatchId(), m);
                                    }

                                    for (int i = 0; i < result.getMatches().getTheirTurnMatches().getCount(); i++) {
                                        TurnBasedMatch m = result.getMatches().getTheirTurnMatches().get(i);
                                        matches.put(m.getMatchId(), m);
                                    }

                                    updateMatchAdapter(gameId);
                                } else {
                                    Log.e(Tag, "Does not have data");
                                }
                            }
                        }
                    });
        }catch (NullPointerException e){
            Log.e(Tag,"Tried to fetch matches from network, but something went wrong ",e);
        }catch (IllegalArgumentException e){
            Log.e(Tag,"Wierd position",e);
        }
    }

    /**
     * Updae the match adapter
     * @param gameId
     */
    private void updateMatchAdapter(int gameId){
        if(d_updateAdapter)Log.e(Tag,"updateMatchAdapter()");
        try {

            if (matchesAdapter != null && gameId != -1) {
                matchesAdapter.clear();
                HashMap<String, TurnBasedMatch> copy = new HashMap<>(matches);
                Iterator it = copy.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    TurnBasedMatch match = (TurnBasedMatch) pair.getValue();
                    if (match.getVariant() == gameId) {

                        //IF TURN_STATUS_COMPLETE ,
                        int status = match.getStatus();
                        int turnStatus = match.getTurnStatus();

                        if (status == TurnBasedMatch.MATCH_STATUS_COMPLETE && turnStatus != TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                            addMatchEntryToListAdapter(match);
                            continue;
                        }
                        //Build the string to our match title
                        if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN || turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN) {
                            addMatchEntryToListAdapter(match);
                        }
                    }
                    it.remove();
                }
            }
        }catch (NullPointerException e){
            Log.e(Tag,"Tried to load matches but null adapter",e);
        }
    }

    /**
     * Adds an entry to our list of matches
     * @param match the TBMP match
     */
    public void addMatchEntryToListAdapter(TurnBasedMatch match){
        StringBuilder builder = new StringBuilder();
        ArrayList<Participant> participants = match.getParticipants();
        for(Participant participant: participants){
            builder.append(participant.getDisplayName() + ", ");
        }
        matchesAdapter.add(new MatchEntry(builder.toString(), match.getTurnStatus(), match.getMatchId()));
    }

    public void removeMatch(String matchId){
        matches.remove(matchId);
    }

    public void updateMatch(TurnBasedMatch match){
        matches.put(match.getMatchId(), match);
    }

    public void addMatch(TurnBasedMatch match){
        matches.put(match.getMatchId(), match);
    }

    public TurnBasedMatch getMatch(String matchId){
        return matches.get(matchId);
    }




}
