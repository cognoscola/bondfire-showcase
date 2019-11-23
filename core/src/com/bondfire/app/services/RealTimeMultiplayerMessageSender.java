package com.bondfire.app.services;

/**
 * Created by alvaregd on 23/02/16.
 * Called by game when we need to send a message
 */
public interface RealTimeMultiplayerMessageSender {

    void OnRealTimeMessageSend(String targetId, String gameData, boolean isReliable);

    void bindReceiver(RealTimeMultiplayerMessageReceiver receiver);

    void CreateGameInvitations();

    void DestroyGameInvitations();

    boolean shouldGoToMultiplayerMenu();

    void setGameConnectionReady();

    void DestroyGameConnection();

    void BroadcastWonRound();

}
