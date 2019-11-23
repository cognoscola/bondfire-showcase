package com.bondfire.app.background.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent

/**
 * Created by alvaregd on 2016-09-17.
 */
class ShapeRenderSystem:IteratingSystem, InputProcessor{
    val TAG: String = ShapeRenderSystem::class.java.name

    private lateinit var shapeRenderableMapper:ComponentMapper<ShapeRenderableComponent>
    private lateinit var renderer:ShapeRenderer
    lateinit var viewport:Viewport

    var INITIAL_DISPLACEMENT:Float = 0.0f
    var distanceBetweenWindowPositions = 0.0f
    companion object{

        val WINDOW_WIDTH = 480
        val WINDOW_HEIGHT = 800

        val WORLD_WIDTH = WINDOW_WIDTH * 2
        val WORLD_HEIGHT = WINDOW_HEIGHT


    }

    constructor():super(Family.all(
            ShapeRenderableComponent::class.java,
            PositionComponent::class.java).get()){

        shapeRenderableMapper = ComponentMapper.getFor(ShapeRenderableComponent::class.java)
        renderer = ShapeRenderer()

        viewport = StretchViewport(WINDOW_WIDTH.toFloat(), WINDOW_HEIGHT.toFloat(), OrthographicCamera())
        viewport.apply()

        INITIAL_DISPLACEMENT = viewport.camera.viewportWidth/2.toFloat()
        distanceBetweenWindowPositions = (WORLD_WIDTH/2 - WINDOW_WIDTH/2).toFloat()


        viewport.camera.position.x = INITIAL_DISPLACEMENT
        viewport.camera.position.y = viewport.camera.viewportHeight/2.toFloat()
        viewport.camera.update()

       Gdx.input.inputProcessor = this
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {

        val shapeComponent = shapeRenderableMapper.get(entity)
        renderer.projectionMatrix = viewport.camera.combined
        shapeComponent.render?.invoke(renderer, deltaTime)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        Gdx.app.log("key up"," Keycode keyUp")

        when (keycode) {
            Input.Keys.LEFT ->{
                Gdx.app.log(ShapeRenderSystem::class.java.name,"keyUp")
                viewport.camera.position.x -=10
                viewport.camera.position.x = MathUtils.clamp(viewport.camera.position.x, 0.0f, (WORLD_WIDTH - WINDOW_WIDTH).toFloat())
                viewport.apply()
                return true
            }
            Input.Keys.RIGHT->{
                viewport.camera.position.x +=10
                viewport.camera.position.x = MathUtils.clamp(viewport.camera.position.x, 0.0f, (WORLD_WIDTH - WINDOW_WIDTH).toFloat())
                viewport.apply()
                return true
            }
            else -> return false
        }
    }

    /**
     * the view is divided into 3 sections. The section we view depends on the position
     * of the page. If we are viewing 1st page, show the left most. If we  are viewing
     * second page, show center, while the 3rd page shows right most
     */
    fun calculateWindowPosition(position:Int, positionOffset:Float){

        when(position){
            //window should be left most, we will never encounter offset with the last view
            2-> viewport.camera.position.x = INITIAL_DISPLACEMENT + WORLD_WIDTH - WINDOW_WIDTH.toFloat()
            //window is between middle and the end. We should
            1-> viewport.camera.position.x = INITIAL_DISPLACEMENT + distanceBetweenWindowPositions + distanceBetweenWindowPositions * positionOffset
            //window is between
            0-> viewport.camera.position.x = INITIAL_DISPLACEMENT + distanceBetweenWindowPositions * positionOffset
        }
        viewport.apply()

/*
        if (position == 2) {
            mGameManager!!.getController()!!.setGraphicsEffectPercent(1f)
        } else if (Position == 1) {
            mGameManager!!.getController()!!.setGraphicsEffectPercent(positionOffset)
        } else if (Position == 0) {
            mGameManager!!.getController()!!.setGraphicsEffectPercent(1f - positionOffset)
        }
*/

    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        Gdx.app.log("key up"," Keycode keyDown")
        when (keycode) {
            Input.Keys.LEFT->{
                return true
            }

            Input.Keys.RIGHT->{
                return true
            }
        }
        return false
    }

    fun dispose(){
        renderer.dispose()
    }

}