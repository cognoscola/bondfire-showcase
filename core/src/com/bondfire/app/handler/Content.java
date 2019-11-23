package com.bondfire.app.handler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.bondfire.app.bfUtils.BlurrableTextureAtlas;
import com.bondfire.app.networkUtils.ImageDownloader;

import java.util.HashMap;

/** Load up files needed by bondfire and games */
public class Content {

    private final static String Tag = Content.class.getName();
    private final static boolean d_LoadProfilePictures = false;
    private final static boolean d_getSmallIcon =false;
    private final static boolean d_getLargeIcon =false;

    /** atlases */
    HashMap<String, BlurrableTextureAtlas> atlases;

    /** shaders */
    HashMap<String, FileHandle> fragmentShaders;
    HashMap<String, FileHandle> vertexShaders;

    /** font generators */
    FreeTypeFontGenerator textGenerator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    BitmapFont bmFont;
    String fontPath;

    /** Profile Images **/
    HashMap<String, TextureRegion> smallIcons;
    HashMap<String, TextureRegion> largeIcons;
    HashMap<String, String> smallIconUrls;
    HashMap<String, String> largeIconUrls;

    public Content(){
        atlases = new HashMap<String, BlurrableTextureAtlas>();
        vertexShaders = new HashMap<String, FileHandle>();
        fragmentShaders = new HashMap<String, FileHandle>();
        smallIcons = new HashMap<String, TextureRegion>();
        largeIcons = new HashMap<String, TextureRegion>();
        smallIconUrls = new HashMap<String, String>();
        largeIconUrls = new HashMap<String, String>();
    }

    /** here we will fetch the assets and put them int memory*/
    public void LoadAtlas(String path, String key){
        atlases.put(key, new BlurrableTextureAtlas(Gdx.files.internal(path)));
    }

    public TextureAtlas getAtlas(String key ){
        return atlases.get(key);
    }

    public void LoadShaders(String vshPath, String fshPath, String key){
        vertexShaders.put(key, Gdx.files.internal(vshPath));
        fragmentShaders.put(key, Gdx.files.internal(fshPath));
    }

    public FileHandle[] getShaders(String key){
        FileHandle[] handles = new FileHandle[2];
        handles[0] =vertexShaders.get(key);
        handles[1] =fragmentShaders.get(key);
        return handles;
    }

    public void LoadFont(String path){
        this.fontPath = path;
    }

    public void GeneratorFont(){

        this.textGenerator = new FreeTypeFontGenerator((Gdx.files.internal(fontPath)));
        this.parameter =     new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 35;
        bmFont  = textGenerator.generateFont(parameter);
        this.textGenerator.dispose();
    }

    public void dispose(){
       if(bmFont != null) bmFont = null;
    }

    public BitmapFont getBmpFont(){
        if(bmFont == null){
            GeneratorFont();
        }
        return bmFont;
    }

    //loads both large and small icons
    public void LoadProfilePictures(String[] small, String[] large){
        smallIconUrls.clear();
        largeIconUrls.clear();

        for(int i =0; i < small.length; i++){
            smallIconUrls.put("p_" + (i+1), small[i]);
            largeIconUrls.put("p_" + (i+1), large[i]);
        }
    }

    public String getLargeIconUrl(int i ){
        return largeIconUrls.get("p_" + i);
    }

    public String getSmallIconUrl(int i ){
        return smallIconUrls.get("p_" + i);
    }

    public void getSmallProfilePicture(final String url, final ImageDownloader.ImageDownloadCallBack callback){

        TextureRegion pic = smallIcons.get(url);
        if(pic == null) {
            if (url != null) {
                if (d_getSmallIcon) System.out.println("Fetching image....");
                ImageDownloader.getImage(url, new ImageDownloader.ImageDownloadCallBack() {
                    @Override
                    public void TextureImageReceived(TextureRegion textureRegion) {
                        smallIcons.put(url,textureRegion);
                        callback.TextureImageReceived(textureRegion);
                    }
                });
            }
        }else{
            if (d_getSmallIcon) System.out.println("Already exists, returning Image");
            callback.TextureImageReceived(pic);
        }
    }

    public void getLargeProfilePicture(final String url, final ImageDownloader.ImageDownloadCallBack callback){

        TextureRegion pic = largeIcons.get(url);
        if(pic == null) {
            if (url != null) {
                if (d_getLargeIcon) System.out.println("Fetching image....");
                ImageDownloader.getImage(url, new ImageDownloader.ImageDownloadCallBack() {
                    @Override
                    public void TextureImageReceived(TextureRegion textureRegion) {
                        largeIcons.put(url,textureRegion);
                        callback.TextureImageReceived(textureRegion);
                    }
                });
            }
        }else{
            if (d_getLargeIcon) System.out.println("Already exists, returning Image");
            callback.TextureImageReceived(pic);
        }
    }

}
