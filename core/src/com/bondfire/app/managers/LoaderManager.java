package com.bondfire.app.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.bondfire.app.engine.Engine;
import com.bondfire.app.loaders.Flower;

/**
 * Created by alvaregd on 01/11/15.
 * Gets a loader Manager
 */
public class LoaderManager {

    Flower flower;
    ShaderProgram colorShader;

    private boolean isShowing;

    public LoaderManager() {
        createLoadingScreen();
    }

    public static LoaderManager getInstance() {
        return new LoaderManager();
    }

    public void showLoadingScreen(){
        this.isShowing = true;
        //TODO reset the loading screen
    }

    public void hideLoadingScreen(){
        this.isShowing = false;
    }

    public void render(SpriteBatch sb){
        if(isShowing){
            sb.setShader(colorShader);
            flower.render(sb, colorShader);
        }
    }

    public void restartAnimation(){

    }

    public void update(float dt){
        if(isShowing){
            flower.update(dt);
        }
    }

    public void createLoadingScreen(){

        ShaderProgram.pedantic = false;
        colorShader = new ShaderProgram(
                Engine.getEngineContent().getShaders(Engine.SHADER_KEY_GRADIENT)[0],
                Engine.getEngineContent().getShaders(Engine.SHADER_KEY_GRADIENT)[1]);

        System.out.println(colorShader.isCompiled() ? "Shader 1 compiled, yay" : colorShader.getLog());

        /** create our objects **/
        flower = new Flower(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        flower.isGrowing(true);
        flower.setSpinRate(4);
        flower.setLeafsPerRevolution(8);
    }


}
