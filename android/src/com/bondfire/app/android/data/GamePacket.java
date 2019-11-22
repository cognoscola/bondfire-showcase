package com.bondfire.app.android.data;

public class GamePacket {

    public GamePacket(String message, int i){
        this.Packet = message;
        this.packetType = i;
        this.completed = false;
    }

    public final static int TYPE_STRING = 0;
    public final static int TYPE_DATA = 1;

    public int packetType;
    public String Packet;
    public boolean completed;
}
