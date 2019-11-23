package com.bondfire.app.services;

/**
 * Created by alvaregd on 23/02/16.
 * called by platform and sent to game when receiving a new game message
 */
public interface RealTimeMultiplayerMessageReceiver {

     void onGameMessageReceived(String string, String senderId);
     void onRoomConfigurationChanged(GameRoom room);
     void onGoToMultiplayerModeCommandReceived();
}
