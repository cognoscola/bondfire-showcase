package com.bondfire.app.handler;

import com.bondfire.app.services.TurnBasedMultiplayerDataPacket;
import com.bondfire.app.services.TurnBasedMultiplayerReceiverListener;

public abstract class TurnBasedEvent {

    private final static String Tag = TurnBasedEvent.class.getName();
    private final static boolean d_clean = true;

    public final static int EVENT_NOT_YOUR_TURN = 1;
    public final static int EVENT_YOUR_TURN = 0;
    public final static int NO_MATCH = 0;

    protected static TurnBasedMultiplayerReceiverListener listener;
    protected static TurnBasedMultiplayerDataPacket data;
    protected static String participantId;
    protected static boolean isFromNetwork;

    public abstract boolean execute();


    public TurnBasedEvent(){

    }

    public static void clean(){
        if(d_clean)System.out.println(Tag + " Cleaned()");
        listener = null;
        data = null;
    }

}
