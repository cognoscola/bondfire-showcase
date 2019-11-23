package com.bondfire.app.progress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.bondfire.app.engine.Engine;
import com.bondfire.app.loaders.Flower;

/**
 * Created by alvaregd on 15/05/16.
 * A progress bar that can be used by the various games within Bondfire
 */
public class BondfireProgressBar  {

    public final static String projectPath = "bondfire";

    private static Flower flower;
    private static ShaderProgram colorShader;

    public static void setIsShowing(boolean isShowing) {
        BondfireProgressBar.isShowing = isShowing;
    }

    private static boolean isShowing;

    /**
     * Loads the assets required to show the progress bar and configures it
     * with the given parameters
     * @param leafsPerRev the number of Leafs to reveal per revolution
     * @param spinRate how long should one revolution take
     */
    public static void prepare(int leafsPerRev, float spinRate){

        ShaderProgram.pedantic = false;

        colorShader = new ShaderProgram(
                Gdx.files.internal("bondfire/gradientColor.vertexsh"),
                Gdx.files.internal("bondfire/gradientColor.fsh"));

        if (!colorShader.isCompiled()) {
            System.out.println(colorShader.isCompiled() ? "Shader 1 compiled, yay" : colorShader.getLog());
        }

        /** create our objects **/
        flower = new Flower(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        flower.isGrowing(true);
        flower.setSpinRate(spinRate);
        flower.setLeafsPerRevolution(leafsPerRev);
        isShowing = true;
    }

    /**
     * Show the progress bar
     * @param dt the time elapsed
     * @param batch the batch that will render the bar
     */
    public static void animate(float dt, PolygonSpriteBatch batch){

        if(isShowing){
            batch.setShader(colorShader);
            flower.render(batch, colorShader);
            flower.update(dt);
        }
    }

    /**
     * Frees up memory used by this class
     */
    public static void dispose() {
        flower.dispose();
        colorShader.dispose();
    }
}
