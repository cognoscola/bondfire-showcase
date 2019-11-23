package com.bondfire.app.services;


public interface TurnBasedMultiplayerReceiverListener {

     void onInvitationReceived();

     void onInvitationRemoved(String s);

     void onTurnBasedMatchReceived(String participantId, boolean isComingFromNetwork, TurnBasedMultiplayerDataPacket data);

     void onTurnBasedMatchRemoved(String s);

}
