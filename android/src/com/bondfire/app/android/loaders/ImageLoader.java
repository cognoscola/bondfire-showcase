package com.bondfire.app.android.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.bondfire.app.android.interfaces.IImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class ImageLoader extends AsyncTask<String, Void, Bitmap>
        implements IImageLoader {

    private final static boolean D = false;

    boolean canW, canR;
    private Context cxt;
    private Bitmap image;

    public ImageLoader(Context context) {
        this.cxt = context;
    }

    @Override
    protected Bitmap doInBackground(String... filename) {

        if (D)
            Log.i("ImageLoader", "Loading " + filename[1]);
        return LoadImageFromFile(filename[0], filename[1]);

    }

    private Bitmap LoadImageFromFile(String path, String filename) {

        if(D)Log.d("LoadImageFromFile", "Path:" + path + " Filename:" + filename);

        InputStream in = null;

        String[] bits = path.split("/");
        // the file name is always the last index of this split
        // Rebuild the path name.

        bits[bits.length - 1] = filename;
        String newPath = "";
        for (int q = 0; q < bits.length; q++) {
            if(D)Log.d("LoadImageFromFile", "PIECE: "+ bits[q]);
            newPath = newPath + bits[q];
            if (q != (bits.length - 1)) {
                newPath = newPath + "/";
            }
        }

        File extStore = cxt.getExternalFilesDir(null);
        File myFile = new File(extStore.getAbsolutePath()
                + "/Games/" + newPath);

        checkState();
        if (canW == canR == true) {

            if (!myFile.exists()) {
                if(D)Log.i("Image Loader", "File " + newPath + " does not exist!");
                return null;
            }

            if(D)Log.i("Image Parser", "File: " + newPath + " exists");
            try {
                in = new FileInputStream(myFile);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
                image = null;
                if(D)Log.i("LoadImage", "BItmap is null!");
                return image;
            }

            image = BitmapFactory.decodeStream(in);
            try {
                in.close();
            } catch (IOException e) {

                e.printStackTrace();
                if(D)Log.i("LoadImage", "Unable to close Input stream");
            }

            return image;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        // this.image = result;
        onImageReceived(result);
    }

    private void checkState() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            canW = canR = true;
        } else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            canW = false;
            canR = true;
        } else {
            canW = canR = false;
        }
    }

}