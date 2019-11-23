package com.bondfire.app.handler;

import com.bondfire.app.services.TurnBasedMultiplayerReceiverListener;


public class EventYourTurn extends TurnBasedEvent {

    public EventYourTurn(String id, boolean isComingFromNetwork, TurnBasedMultiplayerReceiverListener callback){
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
                TurnBasedEvent.listener.onTurnBasedMatchReceived(participantId,isFromNetwork,TurnBasedEvent.data);
                return true;
            }
            else return false;
        }catch (AbstractMethodError e) {
            System.out.println(e);
            return false;
        }
    }
}
