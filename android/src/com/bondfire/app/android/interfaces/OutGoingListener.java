package com.bondfire.app.android.interfaces;

import com.bondfire.app.android.data.BondfirePacket;

public interface OutGoingListener {

    boolean chatSend(BondfirePacket packet);

    boolean gameSend(BondfirePacket packet);

    boolean gameDataSend(BondfirePacket packet);

    public boolean RegisterGCM(BondfirePacket packet);

}
