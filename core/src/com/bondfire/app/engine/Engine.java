package com.bondfire.app.engine;

import com.badlogic.gdx.Gdx;
import com.bondfire.app.handler.Content;
import com.bondfire.app.managers.LoaderManager;

/**
 * Created by alvaregd on 01/11/15
 * The root class of the bondfire engine
 * Decides what should be loaded up when the engine is started
 */

public class Engine {
    private static final String Tag = Engine.class.getName();

    public static final String SHADER_KEY_GRADIENT = "gradientColor";

    private static LoaderManager loaderManager;
    private static Content engineContent;

    public static LoaderManager getLoaderManager() {return loaderManager;}
    public static Content getEngineContent() {return engineContent;}

    public static void init() {
        Gdx.app.log(Tag,"Initializing Engine");
        engineContent = new Content();
        engineContent.LoadShaders("gradientColor.vertexsh","gradientColor.fsh",SHADER_KEY_GRADIENT);
        engineContent.LoadAtlas("flower.pack", "flower");
        loaderManager = LoaderManager.getInstance();
    }

}
