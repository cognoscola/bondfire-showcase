package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
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
class WaterOverlay: Entity {

    private var seconds:Long= 0
    private var percentage: Float = 0.0f

    val konstant  = DaySystem.SECONDS_PER_DAY/2.0f

    companion object{

        val width = ShapeRenderSystem.WORLD_WIDTH
        val height = ShapeRenderSystem.WORLD_HEIGHT * Mountains.mountainBaseHeight

        val colorCloseLight:Color = Color.valueOf("#3366C300")
        val colorCloseDark: Color = Color.valueOf("#111111FF")

        val colorFarLight:Color = Color.valueOf("#3366C3FF")
        val colorFarDark:Color = Color.valueOf("#111111FF")

        val colorFar = Color()
        val colorClose = Color()

        var closeInitialColor = Color()
        var closeFinalColor = Color()

        val farInitialColor = Color()
        val farFinalColor = Color()
    }


    constructor(){
        add(PositionComponent().apply {
            position.x = 0f
            position.y = 0f
        })

        add(ShapeRenderableComponent().apply {
            render = { sr, dt ->

                val positionComponent = getComponent(PositionComponent::class.java)

                Gdx.gl.glEnable(GL20.GL_BLEND)
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                sr.begin(ShapeRenderer.ShapeType.Filled)
                sr.rect(
                        positionComponent.position.x,
                        positionComponent.position.y,
                        WaterOverlay.width.toFloat(), WaterOverlay.height.toFloat(),
                        colorClose, colorClose, colorFar, colorFar
                )

                sr.end()
                Gdx.gl.glDisable(GL20.GL_BLEND)
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

    private fun calculateColor(age:Long){

        seconds = age % DaySystem.SECONDS_PER_DAY

        percentage = this.getPercentage(seconds % (konstant))
        percentage = MathUtils.clamp(percentage,0.0f,1.0f)

        if (seconds >= (DaySystem.SECONDS_PER_DAY * (1.0/2.0))) {

            farFinalColor.set(colorFarDark)
            farInitialColor.set(colorFarLight)

            closeFinalColor.set(colorCloseDark)
            closeInitialColor.set(colorCloseLight)

        } else {
            farFinalColor.set(colorFarLight)
            farInitialColor.set(colorFarDark)

            closeFinalColor.set(colorCloseLight)
            closeInitialColor.set(colorCloseDark)
        }

        colorFar.set(farInitialColor.lerp(farFinalColor,percentage))
        colorClose.set(closeInitialColor.lerp(closeFinalColor,percentage))
    }

    private fun getPercentage(time:Float):Float{
        return time.toFloat() / konstant.toFloat()
    }


}