package com.bondfire.app.android.data;

public class BondfirePacket {

    public BondfirePacket(String source, String destination, String message){

        this.source = source;
        this.destination = destination;
        this.msg = message;
    }

    public void newData(String source, String destination, String message){

        this.source = source;
        this.destination = destination;
        this.msg = message;
    }

    public String source;
    public String destination;
    public String msg;
    public int command = -1;
}

