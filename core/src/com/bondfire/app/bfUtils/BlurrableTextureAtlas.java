package com.bondfire.app.bfUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class BlurrableTextureAtlas extends TextureAtlas {

    Pixmap pixmap;
    public Texture tex;
    private boolean isBlurrable = false;
    private boolean isBound = false;

    public BlurrableTextureAtlas(FileHandle packFile) {
        super(packFile);
    }

    public void PrepareBlur(Pixmap pixmap){

        if(!isBlurrable){
            System.out.println("Blurrable, loading tex");
            checkDimensions(pixmap);
            tex = new Texture(this.pixmap, Pixmap.Format.RGBA8888, false);
            isBlurrable= true;
        }
    }

    public boolean isBlurrable(){
        return isBlurrable;
    }

    private void checkDimensions(Pixmap pixmap){

        int w = pixmap.getWidth(), h = pixmap.getHeight();
        if(w != h) {
            int max = w > h? w : h;
            Pixmap pixmap2 = new Pixmap(max, max, Pixmap.Format.RGBA8888);
            pixmap2.drawPixmap(pixmap, 0, 0);
            this.pixmap = pixmap2;
        }else{
           this.pixmap = pixmap;
        }
    }

    public void bind(){

        if(!isBound){
            tex.bind();
            BlurUtils.generateBlurredMipmaps(pixmap, pixmap.getWidth(), pixmap.getHeight(), 1, 3, true);
            tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            tex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
            isBound = true;

        }
    }
}
