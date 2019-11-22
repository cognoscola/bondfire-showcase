package com.bondfire.app.android.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BondfireDataPacket {

    public BondfireDataPacket(String descriptor, Object object){

        this.descriptor = descriptor;
        this.data = new byte[1];
    }

    public void newData( String descriptor, Object object){

        this.descriptor = descriptor;

        if(object instanceof Bitmap){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ((Bitmap)object).compress(Bitmap.CompressFormat.PNG, 100, baos);
            data = baos.toByteArray();
        }
    }

    public Bitmap getBitmapImage(){
        return BitmapFactory.decodeByteArray(
                data, 0, data.length);

    }

    public String descriptor;
    public byte[] data;



}