package com.bondfire.app.handler;

import com.bondfire.app.services.TurnBasedMultiplayerReceiverListener;

/**
 * Created by alvaregd on 21/07/15.
 * This is a little command thingy which tells the game that we have left the game
 */

public class EventLeaveGame extends TurnBasedEvent {

    public EventLeaveGame(String id, boolean isComingFromNetwork, TurnBasedMultiplayerReceiverListener callback){
        super();

        try {
            listener = callback;
            participantId = id;
            isFromNetwork = isComingFromNetwork;
        }catch (VerifyError e){
            System.out.println("Verify Error... WTF");
        }
    }

    @Override
    public boolean execute() {
        try{
            if(listener != null) {
                System.out.println("Listener not null, calling onTurnBasedMatchReceived()");
                TurnBasedEvent.listener.onTurnBasedMatchRemoved(participantId);
                return true;
            }
            else return false;
        }catch (AbstractMethodError e) {
            System.out.println(e +  "Check installation!!!!!!!");
            return false;
        }
    }
}
