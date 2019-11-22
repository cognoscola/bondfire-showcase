package com.bondfire.app.android.data;

public class ChatMessageEntry {

    public ChatMessageEntry(String message){
        this.message = message;
        this.completed = false;
    }

    public String message;
    public boolean completed;
}
