package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.bondfire.app.background.components.CarcadianComponent
import com.bondfire.app.background.components.DimensionComponent
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent
import com.bondfire.app.background.systems.DaySystem
import com.bondfire.app.background.systems.ShapeRenderSystem

/**
 * Created by alvaregd on 2016-10-23.
 * Paints stars across the sky in a rectangle fashion
 */
class Stars : Entity {

    private val ALPHA_A = 0.5f
    private val ALPHA_B = ((2.0 * 3.14) / DaySystem.SECONDS_PER_DAY).toFloat() // horizontal compression shift
    private val ALPHA_C = 1.571f //in radians
    private val ALPHA_D = 0.5f

    //the sky color, we may use this to draw the milky way or something
    val skyColor = Color(1.0f, 1.0f, 1.0f, 1.0f)


    //the star's positions
    val starPositions = Array<Vector2>()

    constructor() {

        starPositions.clear()

        for (i in 0..30) {
            starPositions.add(Vector2(
                    MathUtils.random(0.0f, ShapeRenderSystem.WINDOW_WIDTH.toFloat() * 2.0f),
                    MathUtils.random(ShapeRenderSystem.WINDOW_HEIGHT * Mountains.mountainBaseHeight, ShapeRenderSystem.WINDOW_HEIGHT.toFloat())))
        }

        add(PositionComponent().apply {
            position.x = 0f
            position.y = Gdx.graphics.height * Mountains.mountainBaseHeight
        })

        add(DimensionComponent().apply {
            dimensions.x = ShapeRenderSystem.WINDOW_WIDTH.toFloat()
            dimensions.y = Gdx.graphics.height * (1 - Mountains.mountainBaseHeight)
        })

        add(CarcadianComponent().apply {
            age = DaySystem.getTime()
            calculateColor(age)

            function = {
                age++
                calculateColor(age)

            }
        })

        add(ShapeRenderableComponent().apply {
            render = { sr, dt ->

//                val position = getComponent(PositionComponent::class.java)
//                val dimensions = getComponent(DimensionComponent::class.java)


                sr.apply {
                    Gdx.gl.glEnable(GL20.GL_BLEND)
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                    begin(ShapeRenderer.ShapeType.Filled)
                    color = skyColor

                    for (position in starPositions) {
                        circle(position.x, position.y, 1.toFloat())
                    }
                    end()
                    Gdx.gl.glDisable(GL20.GL_BLEND)
                }
            }
        })
    }

    private fun calculateColor(age:Long){
        skyColor.a  = ((ALPHA_A * Math.sin(((ALPHA_B * age + ALPHA_C)).toDouble()) + ALPHA_D)).toFloat()
        if (skyColor.a > 0.9) {
            skyColor.a = 0.9f
        }
    }

}