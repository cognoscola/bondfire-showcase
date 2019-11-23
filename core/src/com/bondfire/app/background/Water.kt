package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.bondfire.app.background.components.CarcadianComponent
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent
import com.bondfire.app.background.systems.DaySystem
import com.bondfire.app.background.systems.ShapeRenderSystem


/**
 * Created by alvaregd on 2016-10-13.
 */
class Water: Entity {

    private var seconds:Long= 0
    private var percentage: Float = 0.0f
    private val konstant  = DaySystem.SECONDS_PER_DAY/2.0f

    companion object{

        val width = ShapeRenderSystem.WINDOW_WIDTH * 2f
        val height = Gdx.graphics.height * Mountains.mountainBaseHeight

        val colorCloseLight = Color.valueOf("#36E9E0")
        val colorFarLight = Color.valueOf("#3366C3")

        val colorCloseDark = Color.valueOf("#111111FF")
        val colorFarDark = Color.valueOf("#111111FF")

        val colorCloseInitial = Color()
        val colorCloseFinal = Color()
        val colorFarInitial = Color()
        val colorFarFinal = Color()

        val colorFar = Color()
        val colorClose = Color()
    }

    constructor(){
        add(PositionComponent().apply {
            position.x = 0f
            position.y = 0f
        })

        add(ShapeRenderableComponent().apply {
            render = { sr, dt ->

                val positionComponent = getComponent(PositionComponent::class.java)

                sr.begin(ShapeRenderer.ShapeType.Filled)
                sr.rect(
                        positionComponent.position.x,
                        positionComponent.position.y,
                        width.toFloat(),height.toFloat(),
                        colorClose, colorClose, colorFar, colorFar
                )
                sr.end()
            }
        })

        add(CarcadianComponent().apply {
            age = DaySystem.getTime()
            calculateColor(age)
            function = {
                age++
                calculateColor(age)
            }
        })
    }

    private fun calculateColor(age: Long) {

        seconds = age % DaySystem.SECONDS_PER_DAY

        percentage = this.getPercentage(seconds % (konstant))
        percentage = MathUtils.clamp(percentage,0.0f,1.0f)

        if (seconds >= (DaySystem.SECONDS_PER_DAY * (1.0/2.0))) {

            colorCloseFinal.set(colorCloseDark)
            colorCloseInitial.set(colorCloseLight)

            colorFarFinal.set(colorFarDark)
            colorFarInitial.set(colorFarLight)

        } else {
            colorCloseFinal.set(colorCloseLight)
            colorCloseInitial.set(colorCloseDark)

            colorFarFinal.set(colorFarLight)
            colorFarInitial.set(colorFarDark)
        }

        colorFar.set(colorFarInitial.lerp(colorFarFinal,percentage))
        colorClose.set(colorCloseInitial.lerp(colorCloseFinal, percentage))
    }

    private fun getPercentage(time:Float):Float{
        return time.toFloat() / konstant.toFloat()
    }
}