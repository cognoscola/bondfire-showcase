package com.bondfire.app.services;



import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TurnBasedMultiplayerDataPacket {
    private final static String Tag = TurnBasedMultiplayerDataPacket.class.getName();

    public int gameId;
    public int turnCounter;
    public int playerCount;
    public String gameData;
    public byte[] gameDataBytes;   //everything that is not json format is in here.
    public String[] iconStrings;
    public String[] playerImages;
    public String turn;
    public int gameDataBytesSize;

    //keeps track of the bytes segments we input into gameDataBytes
    public Stack<Integer> dataByteSize;

    public void packBytes(byte[] bytes) {

        try {
            if (bytes == null) {
                return;
            }

            if (dataByteSize == null) {
                dataByteSize = new Stack<Integer>();
            }
            //record the size of the chunk
            dataByteSize.push(bytes.length);

            if (gameDataBytes == null) {
                gameDataBytes = bytes;
            } else {

                byte[] combined = new byte[gameDataBytes.length + bytes.length];
                System.arraycopy(gameDataBytes, 0, combined, 0, gameDataBytes.length);
                System.arraycopy(bytes, 0, combined, gameDataBytes.length, bytes.length);

                gameDataBytes = combined;
            }
        } catch (NullPointerException e) {
            Gdx.app.log(Tag,"Something is null");
        }


    }

    public List<byte[]> unpackNextBytes() {
        try{
            Gdx.app.log(Tag,"Unpacking Heavy Data content");
            List<byte[]> chunks = new ArrayList<byte[]>();
            int totalBacktrackIndex = 0;

            while (!dataByteSize.empty()) {

                //get the data size
                int dataChunkSize = dataByteSize.pop();

                totalBacktrackIndex += dataChunkSize;
                //create a new array with this size
                byte[] chunk = new byte[dataChunkSize];

                //place contents of databytes into this array
                System.arraycopy(gameDataBytes, gameDataBytes.length - totalBacktrackIndex, chunk, 0, dataChunkSize);

                //place this array into the list
                chunks.add(chunk);
            }
            return chunks;
        }catch (NullPointerException e){
            Gdx.app.log(Tag,"Error ocurred while unpacking data byte");
            return null;
        }
    }
}
