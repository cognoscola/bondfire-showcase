package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.bondfire.app.background.components.CarcadianComponent
import com.bondfire.app.background.components.ColorComponent
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent
import com.bondfire.app.background.systems.DaySystem
import com.bondfire.app.background.systems.ShapeRenderSystem

/**
 * Created by alvaregd on 16-09-08.
 */
class Sun:Entity {
    val TAG:String = Sun::class.java.name

    /**
     * Position Properties
     */
    private val SECONDS_PER_DAY = DaySystem.SECONDS_PER_DAY

//    private val A_POSITION_Y = (Gdx.graphics.height/2 ).toFloat() //amplitude
    private val A_POSITION_Y = (ShapeRenderSystem.WORLD_WIDTH * 0.80 ).toFloat() //amplitude
    private val B_POSITION_Y = ((2.0 * 3.14) / SECONDS_PER_DAY).toFloat() // horizontal compression shift
    private val C_POSITION_Y = 4.55f//(B_POSITION_Y * ShevchenkoWallpaper.SECONDS_PER_DAY)//((1/4)*SECONDS_PER_DAY)*B; //phase shift
    private val D_POSITION_Y = 0.0f//Gdx.graphics.height/2//(255 / 2).toFloat() //offset
//    private val D_POSITION_Y = Gdx.graphics.height/2//(255 / 2).toFloat() //offset

    private val A_POSITION_X = (ShapeRenderSystem.WORLD_WIDTH/1.5 ).toFloat() //amplitude
    private val B_POSITION_X = ((2.0 * 3.14) / SECONDS_PER_DAY).toFloat() // horizontal compression shift
    private val C_POSITION_X = 0f//(B_POSITION_Y * ShevchenkoWallpaper.SECONDS_PER_DAY)//((1/4)*SECONDS_PER_DAY)*B; //phase shift
    private val D_POSITION_X = ShapeRenderSystem.WORLD_WIDTH/1.5

    /**
     * Sun Ray Properties
     */
    private val PERIOD_SECONDS = 32.0f

    val MAX_RADIUS = 50.0f
    val MIN_RADIUS = 25.0f

    private val A_SUN_RAY = 25.toFloat()
    private val B_SUN_RAY = ((2.0 * 3.14) / PERIOD_SECONDS).toFloat()
    private val C_SUN_RAY = (B_SUN_RAY * PERIOD_SECONDS)
    private val D_SUN_RAY = 25.0.toFloat()

    private var rayAge:Float = 0.0f

    private var ray1Radius:Float = 0.0f
    private var ray2Radius:Float = 0.0f
    private var ray3Radius:Float = 0.0f
    private var ray4Radius:Float = 0.0f

    var centerRadius  = 25.0f

    val circleOneColor: Color = Color(
            237/ColorComponent.CHARMAX,
            240/ColorComponent.CHARMAX,
            209/ColorComponent.CHARMAX,
            0.1f)
    val circleTwoColor: Color = Color(
            237/ColorComponent.CHARMAX,
            240/ColorComponent.CHARMAX,
            209/ColorComponent.CHARMAX,
            0.3f)
    val circleThreeColor: Color = Color(
            237/ColorComponent.CHARMAX,
            240/ColorComponent.CHARMAX,
            209/ColorComponent.CHARMAX,
            0.4f)
    val circleFourColor: Color = Color(
            237/ColorComponent.CHARMAX,
            240/ColorComponent.CHARMAX,
            209/ColorComponent.CHARMAX,
            0.4f)
    val centerColor: Color = Color(
            240/ColorComponent.CHARMAX,
            239/ColorComponent.CHARMAX,
            120/ColorComponent.CHARMAX,
            1.0f)

    val AMPLITUDE_CONSTANT =  -2 / 3.14f
    val RADIUS_AMPLITUDE =  A_SUN_RAY * AMPLITUDE_CONSTANT
    val ALPHA_AMPLITUDE = 0.5f * AMPLITUDE_CONSTANT
    val ALPHA_D = 0.5f

    val T_CONST = 3.14f/ PERIOD_SECONDS

    //Center radius Constants
    private val A_CENTER_RADIUS = 2.0.toFloat()
    private val B_CENTER_RADIUS = ((2.0 * 3.14) / 16.0f).toFloat()
    private val C_CENTER_RADIUS = 0.0f
    private val D_CENTER_RADIUS = 25.0.toFloat()

    constructor(){

        add(PositionComponent())
        add(CarcadianComponent().apply {

            age = DaySystem.getTime()
            calculatePosition(age)

            function = {
                age++
                calculatePosition(age)
            }
        })

        add(ShapeRenderableComponent().apply {
            render = { renderer, dt ->

                val positionComponent = getComponent(PositionComponent::class.java)

                rayAge += dt

                circleOneColor.a = 1 - ALPHA_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST))).toFloat() + ALPHA_D
                ray1Radius = RADIUS_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST))).toFloat() + D_SUN_RAY

                circleTwoColor.a = 1 - ALPHA_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST - 1.047))).toFloat() + ALPHA_D
                ray2Radius = RADIUS_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST - 1.047))).toFloat() + D_SUN_RAY

                circleThreeColor.a = 1 - ALPHA_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST - 2.094))).toFloat() + ALPHA_D
                ray3Radius = RADIUS_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST - 2.094))).toFloat() + D_SUN_RAY

                circleFourColor.a = 1 - ALPHA_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST - 3.14))).toFloat() + ALPHA_D
                ray4Radius = RADIUS_AMPLITUDE * Math.atan((1 / Math.tan(rayAge.toDouble() * T_CONST - 3.14))).toFloat() + D_SUN_RAY

                centerRadius = (A_CENTER_RADIUS * Math.sin((4.0f * (B_CENTER_RADIUS * rayAge - C_CENTER_RADIUS).toFloat()).toDouble()) + D_CENTER_RADIUS).toFloat()

                Gdx.gl.glEnable(GL20.GL_BLEND)
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                
                renderer.begin(ShapeRenderer.ShapeType.Filled)

                if (ray1Radius < 49.0) {
                    renderer.color = circleOneColor
                    renderer.circle(positionComponent.position.x, positionComponent.position.y, ray1Radius)
                }

                if (ray2Radius < 49.0f) {
                    renderer.color = circleTwoColor
                    renderer.circle(positionComponent.position.x, positionComponent.position.y, ray2Radius)
                }

                if (ray3Radius < 49.0f) {
                    renderer.color = circleThreeColor
                    renderer.circle(positionComponent.position.x, positionComponent.position.y, ray3Radius)
                }

                if (ray4Radius < 49.0f) {
                    renderer.color = circleFourColor
                    renderer.circle(positionComponent.position.x, positionComponent.position.y, ray4Radius)
                }

                renderer.color = centerColor
                renderer.circle(positionComponent.position.x, positionComponent.position.y, centerRadius)

                renderer.end()
                Gdx.gl.glDisable(GL20.GL_BLEND)
                this
            }
        })
    }

    private fun calculatePosition(age:Long){
        val position = getComponent(PositionComponent::class.java).position
        //                this.position.x = Gdx.graphics.width/2.toFloat()
        //                this.position.y = Gdx.graphics.height/2.toFloat()
        position.x = ((A_POSITION_X * Math.sin(((B_POSITION_X * age + C_POSITION_X)).toDouble()) + D_POSITION_X)).toFloat()
        position.y = ((A_POSITION_Y * Math.sin(((B_POSITION_Y * age + C_POSITION_Y)).toDouble()) + D_POSITION_Y)).toFloat()
        this
    }
}