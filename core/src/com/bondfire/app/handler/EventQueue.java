package com.bondfire.app.handler;


import com.bondfire.app.services.TurnBasedMultiplayerDataPacket;
import com.bondfire.app.services.TurnBasedMultiplayerReceiverListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class EventQueue {

    private final static String Tag = EventQueue.class.getName();
    private final static boolean d_clean = true;

    static Queue<TurnBasedEvent> turnBasedEventQueue;
    static ArrayList<String> participants;

    public EventQueue(){
        turnBasedEventQueue = new LinkedList<TurnBasedEvent>();
        participants = new ArrayList<String>();
    }

    public void push(TurnBasedEvent turnBasedEvent){
        if(turnBasedEventQueue == null){
            turnBasedEventQueue = new LinkedList<TurnBasedEvent>();
        }
        turnBasedEventQueue.add(turnBasedEvent);
        //Attempt to do this event
    }

    public TurnBasedEvent getEvent(){
        return turnBasedEventQueue.peek();
    }

    public void destroyEvent(){
        turnBasedEventQueue.remove();
    }

    public int getSize(){
        return turnBasedEventQueue.size();
    }

    public boolean isEmpty(){
        return turnBasedEventQueue.size() == 0;
    }

    public void UpdateData(TurnBasedMultiplayerDataPacket data){
        TurnBasedEvent.data = data;
    }

    public TurnBasedMultiplayerDataPacket getData(){
        return TurnBasedEvent.data;
    }

    public void UpdateCallBacks(TurnBasedMultiplayerReceiverListener listener){
            TurnBasedEvent.listener = listener;
    }

    public boolean ExecuteAll() {
        boolean ret = true;
        while (turnBasedEventQueue.size() != 0) {
            TurnBasedEvent turnBasedEvent = getEvent();

            if (!turnBasedEvent.execute()) {
                System.out.println("Unable to call Listener... shit");
                ret = false;

                break;
            } else destroyEvent();
        }

        return ret;
    }

    public void ExecuteNext(){

    }

    public static void clean(){
        if(d_clean)System.out.println(Tag + " Cleaned()");
        turnBasedEventQueue = null;
    }

    public void updateParticipants(ArrayList<String> players){
        participants = players;

    }

    public ArrayList<String> getParticipants(){
        return participants;
    }

}
