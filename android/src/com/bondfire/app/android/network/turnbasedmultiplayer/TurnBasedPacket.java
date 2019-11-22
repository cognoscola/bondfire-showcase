package com.bondfire.app.android.network.turnbasedmultiplayer;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.bondfire.app.services.TurnBasedMultiplayerDataPacket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TurnBasedPacket extends TurnBasedMultiplayerDataPacket {

    private final static  String Tag = TurnBasedPacket.class.getName();
    private final static String KEY_GAMEID      = "gameid";
    private final static String KEY_GAMEDATA    = "gameData";
    private final static String KEY_TURNCOUNTER = "turnCounter";
    private final static String KEY_PLAYERCOUNT = "playerCount";
    private final static String KEY_ICON =  "playerIcons";
    private final static String KEY_IMAGES = "playerImages";
    private final static String KEY_DATASIZE = "dataSize";

    public static Json json;

    public TurnBasedPacket(){
        if(json == null)
        json = new Json();
    }

    // This is the byte array we will write out to the TBMP API.
    public byte[] persist() {

        JSONObject retVal = new JSONObject();
        this.gameDataBytesSize = gameDataBytes.length;
        try {
            retVal.put(KEY_GAMEID,      this.gameId);
            retVal.put(KEY_GAMEDATA,    this.gameData);
            retVal.put(KEY_TURNCOUNTER, this.turnCounter);
            retVal.put(KEY_PLAYERCOUNT, this.playerCount);
            retVal.put(KEY_ICON,json.toJson(iconStrings));
            retVal.put(KEY_IMAGES, json.toJson(playerImages));
            retVal.put(KEY_DATASIZE, json.toJson(this.gameDataBytesSize));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String st = retVal.toString();
        Log.d(Tag, "==== PERSISTING\n" + st);

        //We don't want to Jsonify the byte[] arrays in our game data (because it would
        //increase the payload by 3 times as much!) so just concatenate it to the
        //resulting ret value.

        byte[] metadata = st.getBytes(Charset.forName("UTF-8"));
        byte[] metaDataLength = ByteBuffer.allocate(4).putInt(metadata.length).array();
        byte[] ret = new byte[4 + metadata.length + this.gameDataBytes.length]; //we assign 4 bytes to indicate the size of our metadata
        System.arraycopy(metaDataLength, 0, ret, 0, metaDataLength.length);
        System.arraycopy(metadata, 0, ret, 4, metadata.length);
        System.arraycopy(gameDataBytes, 0, ret, 4 + metadata.length, this.gameDataBytes.length);

        Gdx.app.log(Tag, "SIZE OF PACKET AFTER PERSISTING: " + ret.length);
        return ret;
    }

    // Creates a new instance of SkeletonTurn.
    static public TurnBasedMultiplayerDataPacket unpersist(byte[] byteArray) {

        if (byteArray == null) {
            Log.d(Tag, "Empty array---possible bug.");
            TurnBasedPacket nullpacket = new TurnBasedPacket();
            nullpacket.gameId = -1;
            return nullpacket;
        }

        //before we can do anything,
        //we must break up the byte array. We get the first 4 bytes to tell us the size
        byte[] metaDataSizeBytes = new byte[4];
        System.arraycopy(byteArray, 0, metaDataSizeBytes, 0, 4);
        int metaDataSize = ByteBuffer.wrap(metaDataSizeBytes).getInt();

        byte[] metadata = new byte[metaDataSize];

        System.arraycopy(byteArray, 4, metadata, 0, metaDataSize);

        String st = null;
        try {
            st = new String(metadata, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(Tag, "====UNPERSIST \n" + st);
        TurnBasedMultiplayerDataPacket retVal = new TurnBasedPacket();

        try {
            retVal.gameDataBytes = new byte[byteArray.length - metaDataSize - 4];
            System.arraycopy(byteArray, 4 + metaDataSize, retVal.gameDataBytes, 0, metaDataSize);
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.log(Tag, " heavy byte array handling fuck up");
        } catch (NullPointerException e) {
            Gdx.app.log(Tag,"Something is null");
        }

        try {
            JSONObject obj = new JSONObject(st);

            if (obj.has(KEY_GAMEID)) {
                retVal.gameId = obj.getInt(KEY_GAMEID);
            }

            if (obj.has(KEY_TURNCOUNTER)) {
                retVal.turnCounter = obj.getInt(KEY_TURNCOUNTER);
            }

            if (obj.has(KEY_GAMEDATA)) {
                retVal.gameData = obj.getString(KEY_GAMEDATA);
            }
            if (obj.has(KEY_PLAYERCOUNT)){
                retVal.playerCount = obj.getInt(KEY_PLAYERCOUNT);
            }

            if(obj.has(KEY_ICON)){
                retVal.iconStrings = json.fromJson(String[].class, obj.getString(KEY_ICON));
            }
            if(obj.has(KEY_IMAGES)){
                retVal.playerImages = json.fromJson(String[].class, obj.getString(KEY_IMAGES));
            }
            if (obj.has(KEY_DATASIZE)) {
                retVal.gameDataBytesSize = obj.getInt(KEY_DATASIZE);
            }

            //get our values.
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retVal;
    }
}
