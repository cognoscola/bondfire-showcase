package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent
import com.bondfire.app.background.systems.ShapeRenderSystem

/**
 * Created by alvaregd on 2016-10-13.
 */
class Wind:Entity{
    val TAG: String = Wind::class.java.name

    /*** color of the edge of the wind rectangle*/
    val edgeColor = Color(1.0f, 1.0f, 1.0f, 0.0f)

    /*** colr of the middle part of the wind rectangel*/
    val middleColor = Color(1.0f, 1.0f, 1.0f, 0.3f)

    /** color to use for debug purposes */
    val debugColor = Color(1.0f,0.0f,0.0f,1.0f)

    val MAX_WIND_RAY_HALF_WIDTH = 100f

    /** how fast the ray will travel */
    var lifeSpan = 3.0f

//    val lifeSpan = 14.0f

    val MAX_DT = ShapeRenderSystem.WORLD_WIDTH + MAX_WIND_RAY_HALF_WIDTH * 2
    var windVelocity = MAX_DT / lifeSpan

    val WIND_RAY_SPACING = ShapeRenderSystem.WORLD_HEIGHT * 0.02

    var TEMP_X_OFFSET_SCALAR = 0.0f

    val lineCount = Array<WindRay>(3)
    val inactiveLines = Array<WindRay>(3)

    data class WindRay(
            var offsetX:Float,
            var offsetY:Float,
            var halfW:Float,
            var h:Float
    )

    constructor (){

        inactiveLines.add(WindRay(0.0f,0.0f,0.0f,0.0f))
        inactiveLines.add(WindRay(0.0f,0.0f,0.0f,0.0f))
        inactiveLines.add(WindRay(0.0f,0.0f,0.0f,0.0f))

        add(PositionComponent().apply {
            position.x = 0 - MAX_WIND_RAY_HALF_WIDTH
            position.y = ShapeRenderSystem.WORLD_HEIGHT/2.toFloat()
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

                    for (item in lineCount) {

                        //draw the first half of the wind
                        rect(
                                positionComponent.position.x + item.offsetX,
                                positionComponent.position.y + item.offsetY,
                                item.halfW,
                                item.h,
                                edgeColor, middleColor, middleColor, edgeColor
                        )
                        //draw the other half
                        rect(
                                positionComponent.position.x + item.offsetX + item.halfW,
                                positionComponent.position.y + item.offsetY,
                                item.halfW,
                                item.h,
                                middleColor, edgeColor, edgeColor, middleColor
                        )
                    }
                    end()
                    Gdx.gl.glDisable(GL20.GL_BLEND)
                }
            }
        })
    }

    /**
     * reset the wind coordinates
     */
    fun reset(positionComponent: PositionComponent){

        //pick the number of wind lines to draw
        while (lineCount.size > 0) {
            inactiveLines.add(lineCount.pop())
        }

        var rayCount = MathUtils.random(0,3)
        while (rayCount > 0 && inactiveLines.size > 0) {
            lineCount.add(inactiveLines.pop())
            rayCount--
        }

        //for each item in the new list, randomize the height
        var offsetCount = 0.0f

        for (item in lineCount) {
            item.h = ShapeRenderSystem.WORLD_HEIGHT * MathUtils.random(0.01f,0.03f)
            item.halfW = ShapeRenderSystem.WORLD_WIDTH * 0.15f

            //x will be relative to the entity position
            item.offsetX = ShapeRenderSystem.WORLD_WIDTH * MathUtils.random(0.00f,0.15f)

            //offset is determined based on the position in the array
            item.offsetY = offsetCount
            offsetCount += item.h
            offsetCount += WIND_RAY_SPACING.toFloat()
        }

        positionComponent.position.x = 0 - (MAX_WIND_RAY_HALF_WIDTH * 2) - ShapeRenderSystem.WORLD_WIDTH *0.05f
        positionComponent.position.y = ShapeRenderSystem.WORLD_HEIGHT * MathUtils.random(Mountains.mountainBaseHeight, 0.94f)

        lifeSpan = MathUtils.random(3.0f,5.0f)
        windVelocity  = MAX_DT / lifeSpan
    }
}