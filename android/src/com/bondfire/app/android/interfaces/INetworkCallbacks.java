package com.bondfire.app.android.interfaces;

import com.bondfire.app.android.data.BondfirePacket;
import com.bondfire.app.android.data.ChatPacket;

public interface INetworkCallbacks {

    void OnJoinParty(String username);
    void OnUpdateUserList(String users);
    void OnLeaveParty(String username);
    void OnPrepareforInvite(BondfirePacket packet);
    void OnDisband(String username);
    void OnAppointLeader(BondfirePacket packet);




    void OnDisconnect();
    void OnHandshakeFinished(String Message);

    void OnMessageReceived(ChatPacket chatpacket);
    void onGameDataReceived(ChatPacket chatpacket);
    void onGameData2Received(ChatPacket packet);

}