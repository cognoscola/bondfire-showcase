package com.bondfire.app.background

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine

import com.badlogic.gdx.Gdx
import com.bondfire.app.background.systems.BatchRenderSystem
import com.bondfire.app.background.systems.DaySystem
import com.bondfire.app.background.systems.ShapeRenderSystem

/**
 * Created by alvaregd on 16-09-07.
 */
class ShevchenkoWallpaper {

    private lateinit var gradientBackground: GradientBoardV2
    private lateinit var sun: Sun
    private lateinit var mountains: Mountains
    private lateinit var water: Water
    private lateinit var waterOverlay: WaterOverlay

    private var ashley:Engine?= null

    companion object {

    }

    constructor(ashley: Engine?) {

        this.ashley = ashley

        ashley?.addSystem(DaySystem())
        ashley?.addSystem(ShapeRenderSystem())
        ashley?.addSystem(BatchRenderSystem())

        //make the gradient 3 times the width of the screen
        ashley?.addEntity(GradientBoardV2())
        ashley?.addEntity(Stars())
        ashley?.addEntity(Sun())
        ashley?.addEntity(Water())

        //TODO
        //        ashley?.addEntity(Auroras())

        ashley?.addEntity(Mountains())
        ashley?.addEntity(Wave(false))
        ashley?.addEntity(Wave(true))
        ashley?.addEntity(WaterOverlay())
        ashley?.addEntity(Wind())
        ashley?.addEntity(Wind())
    }

    fun dispose(){

        ashley?.getSystem(ShapeRenderSystem::class.java)?.dispose()
        ashley?.getSystem(BatchRenderSystem::class.java)?.dispose()
    }
}