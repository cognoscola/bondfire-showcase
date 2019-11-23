package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent
import com.bondfire.app.background.systems.ShapeRenderSystem

/**
 * Created by alvaregd on 2016-10-15.
 */
class Wave :Entity{
    val TAG: String = Wind::class.java.name

    val debugColor = Color(1.0f,0.0f,0.0f,1.0f)

    val tempColor = Color()
    var tempConst = 0.0f

    val MAX_WIND_RAY_HALF_WIDTH = 100f

    /** how fast the ray will travel */
    var lifeSpan = 3.0f

//    val lifeSpan = 14.0f

    val MAX_DT = Gdx.graphics.width + MAX_WIND_RAY_HALF_WIDTH * 2

    var windVelocity = MAX_DT / lifeSpan

    val WAVE_SPACING_CONSTANT = 0.02
    val WIND_RAY_SPACING = Gdx.graphics.height * WAVE_SPACING_CONSTANT

    val MAX_WAVE_HEIGHT_CONSTANT = 0.03f

    val lineCount = Array<WaveRay>(3)
    val inactiveLines = Array<WaveRay>(3)

    var isDepthWave:Boolean = false


    data class WaveRay(
            var offsetX:Float, //the distance to offset wind in X direction
            var offsetY:Float, //the
            var halfW:Float,
            var h:Float,
            var closeEdgeColor: Color,
            var closeMiddleColor: Color,
            var farMiddleColor: Color,
            var farEdgeColor: Color
    )

    constructor (depthWave:Boolean){

        this.isDepthWave = depthWave

        inactiveLines.add(WaveRay(0.0f, 0.0f, 0.0f, 0.0f,Color(),Color(),Color(),Color()))
        inactiveLines.add(WaveRay(0.0f, 0.0f, 0.0f, 0.0f,Color(),Color(),Color(),Color()))
        inactiveLines.add(WaveRay(0.0f, 0.0f, 0.0f, 0.0f,Color(),Color(),Color(),Color()))

        add(PositionComponent().apply {
            position.x = 0 - MAX_WIND_RAY_HALF_WIDTH
            position.y = ShapeRenderSystem.WORLD_HEIGHT/2.toFloat()

            reset(this)
        })

        add(ShapeRenderableComponent().apply {
            render = { sr, dt ->

                val positionComponent = getComponent(PositionComponent::class.java)
                positionComponent.position.x += windVelocity * dt
                if (positionComponent.position.x > (MAX_DT)) reset(positionComponent)

                sr.apply {
                    Gdx.gl.glEnable(GL20.GL_BLEND)
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                    begin(ShapeRenderer.ShapeType.Filled)

                    for ((
                            offsetX,offsetY, halfW, h,
                            closeEdgeColor,closeMiddleColor,farMiddleColor,farEdgeColor) in lineCount) {

                        //draw the first half of the wind
                        rect(
                                positionComponent.position.x + offsetX,
                                positionComponent.position.y + offsetY,
                                halfW,
                                h,
                                closeEdgeColor, closeMiddleColor, farMiddleColor, farEdgeColor
                        )
                        //draw the other half
                        rect(
                                positionComponent.position.x + offsetX + halfW,
                                positionComponent.position.y + offsetY,
                                halfW,
                                h,
                                closeMiddleColor, closeEdgeColor, farEdgeColor, farMiddleColor
                        )
                    }
                    end()
                    Gdx.gl.glDisable(GL20.GL_BLEND)


/*
                    begin(ShapeRenderer.ShapeType.Line)
                    for ((offsetX,offsetY, halfW, h,edgeColor,middleColor) in lineCount) {

                        //draw the first half of the wind
                        rect(
                                positionComponent.position.x + offsetX,
                                positionComponent.position.y + offsetY,
                                halfW,
                                h,
                                debugColor, debugColor, debugColor, debugColor
                        )
                        //draw the other half
                        rect(
                                positionComponent.position.x + offsetX + halfW,
                                positionComponent.position.y + offsetY,
                                halfW,
                                h,
                                debugColor, debugColor, debugColor, debugColor
                        )

                    }
                    end()
*/
                }
            }
        })
    }

    /**
     * reset the wind coordinates
     */
    fun reset(positionComponent: PositionComponent){

        positionComponent.position.x = 0 - (MAX_WIND_RAY_HALF_WIDTH * 2) - ShapeRenderSystem.WORLD_WIDTH *0.05f
//        positionComponent.position.x = Gdx.graphics.width/2.toFloat()

        //find a position between the base and the max height, except we must also account for the
        //bottom/left centric drawings.
        positionComponent.position.y = ShapeRenderSystem.WORLD_HEIGHT * MathUtils.random(0.0f, ( Mountains.mountainBaseHeight - WAVE_SPACING_CONSTANT * 2  - MAX_WAVE_HEIGHT_CONSTANT * 3).toFloat())

        //pick the number of wind lines to draw
        while (lineCount.size > 0) {
            inactiveLines.add(lineCount.pop())
        }

        var rayCount = MathUtils.random(0,3)
        Gdx.app.log(TAG,"reset generated $rayCount rays")
        while (rayCount > 0 && inactiveLines.size > 0) {
            lineCount.add(inactiveLines.pop())
            rayCount--
        }

        //for each item in the new list, randomize the height
        var offsetCount = 0.0f

        for (item in lineCount) {


//            item.h = Gdx.graphics.height * MathUtils.random(0.01f,0.2f)
            item.h = ShapeRenderSystem.WORLD_HEIGHT * MathUtils.random(0.01f,0.03f)
            item.halfW = ShapeRenderSystem.WORLD_WIDTH * 0.15f

            //x will be relative to the entity position
            item.offsetX = ShapeRenderSystem.WORLD_WIDTH * MathUtils.random(0.00f,0.15f)

            //offset is determined based on the position in the array
            item.offsetY = offsetCount


            //determine the color of the wave based on the height
            //color is determined based on Y position with the water.
            tempConst = (positionComponent.position.y + item.offsetY) / (ShapeRenderSystem.WORLD_HEIGHT * Mountains.mountainBaseHeight)
            tempColor.set(Water.colorCloseLight)
            tempColor.lerp(Water.colorFarLight,tempConst)

            item.closeEdgeColor.set(tempColor).a = 0.0f
            item.closeMiddleColor.set(if(isDepthWave) tempColor else Water.colorCloseLight).a = 0.3f

            tempConst =  (positionComponent.position.y + item.offsetY + item.h) / (ShapeRenderSystem.WORLD_HEIGHT * Mountains.mountainBaseHeight)
            tempColor.set(Water.colorCloseLight)
            tempColor.lerp(Water.colorFarLight,tempConst)
            item.farEdgeColor.set(tempColor).a = 0.0f
            item.farMiddleColor.set(if(isDepthWave) tempColor else Water.colorCloseLight).a = 0.3f


            offsetCount += item.h
            offsetCount += WIND_RAY_SPACING.toFloat()
        }

        lifeSpan = MathUtils.random(3.0f,5.0f)
        windVelocity  = MAX_DT / lifeSpan
    }
}