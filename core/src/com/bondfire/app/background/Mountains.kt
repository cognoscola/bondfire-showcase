package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.*
import com.bondfire.app.background.components.CarcadianComponent
import com.bondfire.app.background.components.ColorComponent
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent
import com.bondfire.app.background.systems.DaySystem
import com.bondfire.app.background.systems.ShapeRenderSystem

/**
 * Created by alvaregd on 2016-10-09.
 */

class Mountains:Entity{
    val TAG: String = Mountains::class.java.name


    private val mountainOneBrightColor = Color.valueOf("#4B7DD6")
    private val mountainTwoBrightColor = Color.valueOf("#5C90E7")
    private val mountainThreeBrightColor = Color.valueOf("#6A9FF9")

    private val mountainOneDarkColor = Color.valueOf("#111111")
    private val mountainTwoDarkColor = Color.valueOf("#111111")
    private val mountainThreeDarkColor = Color.valueOf("#111111")

    private val mountainReflectionDarkColor = Color.valueOf("#111111")

    //the mountain to perform calculations with
    private val mountainOneInitialColor = Color()
    private val mountainOneFinalColor = Color()

    private val mountainTwoInitialColor = Color()
    private val mountainTwoFinalColor = Color()

    private val mountainThreeInitialColor = Color()
    private val mountainThreeFinalColor = Color()

    private val mountainReflectionInitialColor = Color()
    private val mountainReflectionFinalColor = Color()


    //the calculated mountain color
    private val mountainOneColor = Color()
    private val mountainTwoColor = Color()
    private val mountainThreeColor = Color()

    private val mountainReflectionColor = Color()

    private val mountainReflectionFar = Water.colorFarLight
    private val mountainReflectionClose = Water.colorCloseLight

    private val controlPointColor = Color(1.0f,0.0f,0.0f,1.0f)
    private val baseFanColor = Color(0.0f,0.0f,1.0f,1.0f)

    var SAMPLE_POINTS = 100
    var SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS

    var w = ShapeRenderSystem.WINDOW_WIDTH * 2.0f
    var h = ShapeRenderSystem.WINDOW_HEIGHT


    val tmpV = Vector2()

    val basePoint =  Vector2(w * 0.50f, h*mountainBaseHeight)

    var seconds: Long = 0
    var percentage: Float=0.0f

    constructor(){

        val controlPointsOne = arrayOf<Vector2>(
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2( 0.10f * w, 0.35f * h),
                Vector2( 0.30f* w, 0.5f  * h),
                Vector2(0.55f   * w ,0.3145f * h),
                Vector2(0.750f * w ,0.55f  * h),
                Vector2(0.90f  * w, 0.35f * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h)
        )

        val controlPointsTwo = arrayOf<Vector2>(
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2( 0.10f * w, 0.38f * h),
                Vector2( 0.30f* w, 0.53f  * h),
                Vector2(0.55f   * w ,0.30f * h),
                Vector2(0.750f * w ,0.63f  * h),
                Vector2(0.90f  * w, 0.38f * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h)
        )

        val controlPointsThree = arrayOf<Vector2>(
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2( 0.10f * w, 0.41f * h),
                Vector2( 0.30f * w,  0.56f  * h),
                Vector2(0.55f  * w ,0.33f * h),
                Vector2(0.750f * w ,0.66f  * h),
                Vector2(0.90f  * w, 0.41f * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h)
        )

        val controlPointsReflection = arrayOf<Vector2>(
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2(-0.10f * w, mountainBaseHeight * h),
                Vector2( 0.10f * w, 0.275f * h),
                Vector2( 0.30f* w, 0.14f  * h),
                Vector2(0.55f   * w ,0.28625f * h),
                Vector2(0.750f * w ,0.05625f * h),
                Vector2(0.90f  * w, 0.275f * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h),
                Vector2(1.10f  * w, mountainBaseHeight * h)
        )


        val splineOne = BSpline<Vector2>(controlPointsOne,3,false)
        val splineTwo = BSpline<Vector2>(controlPointsTwo,3,false)
        val splineThree = BSpline<Vector2>(controlPointsThree,3,false)
        val splineReflection = BSpline<Vector2>(controlPointsReflection,3,false)


        add(PositionComponent().apply {
            positionFunction = {
                this.position.x = (ShapeRenderSystem.WINDOW_WIDTH /2).toFloat()
                this.position.y = (ShapeRenderSystem.WINDOW_HEIGHT /2).toFloat()
                this
            }
        })

        add(ShapeRenderableComponent().apply {
            render = { shapeRenderer, dt ->


                Gdx.gl.glEnable(GL20.GL_BLEND)
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

                drawMountain(shapeRenderer, splineThree, mountainThreeColor, mountainThreeColor)
                drawMountain(shapeRenderer, splineTwo, mountainTwoColor, mountainTwoColor)
                drawMountain(shapeRenderer, splineOne, mountainOneColor, mountainOneColor)
                drawMountain(shapeRenderer, splineReflection, mountainReflectionColor,mountainReflectionColor)

/*
                shapeRenderer.apply {
                    begin(ShapeRenderer.ShapeType.Filled)
                    this.color = controlPointColor
                    for (value in controlPointsOne) {
                        this.circle(value.x, value.y, 5.0f)
                    }

                    color = baseFanColor
                    circle(basePoint.x, basePoint.y, 5.0f)


                    end()
                }
*/
                Gdx.gl.glDisable(GL20.GL_BLEND)
                this
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

    companion object{
        val mountainBaseHeight = 0.315f
    }

    private fun calculateColor(age:Long){
        val colorComponent = getComponent(ColorComponent::class.java)

        seconds = age % DaySystem.SECONDS_PER_DAY

        percentage = this.getPercentage(seconds % (konstant))
        percentage = MathUtils.clamp(percentage,0.0f,1.0f)

        if (seconds >= (DaySystem.SECONDS_PER_DAY * (1.0/2.0))) {

            mountainOneFinalColor.set(mountainOneDarkColor)
            mountainOneInitialColor.set(mountainOneBrightColor)

            mountainTwoFinalColor.set(mountainTwoDarkColor)
            mountainTwoInitialColor.set(mountainTwoBrightColor)

            mountainThreeFinalColor.set(mountainThreeDarkColor)
            mountainThreeInitialColor.set(mountainThreeBrightColor)

            mountainReflectionFinalColor.set(mountainReflectionDarkColor)
            mountainReflectionInitialColor.set(mountainReflectionClose)

        } else {
            mountainOneFinalColor.set(mountainOneBrightColor)
            mountainOneInitialColor.set(mountainOneDarkColor)

            mountainTwoFinalColor.set(mountainTwoBrightColor)
            mountainTwoInitialColor.set(mountainTwoDarkColor)

            mountainThreeFinalColor.set(mountainThreeBrightColor)
            mountainThreeInitialColor.set(mountainThreeDarkColor)

            mountainReflectionFinalColor.set(mountainReflectionClose)
            mountainReflectionInitialColor.set(mountainReflectionDarkColor)

        }

        mountainOneColor.set(mountainOneInitialColor.lerp(mountainOneFinalColor,percentage))
        mountainTwoColor.set(mountainTwoInitialColor.lerp(mountainTwoFinalColor,percentage))
        mountainThreeColor.set(mountainThreeInitialColor.lerp(mountainThreeFinalColor,percentage))

        mountainReflectionColor.set(mountainReflectionInitialColor.lerp(mountainReflectionFinalColor,percentage))
    }


    fun drawMountain(sr:ShapeRenderer, spline:BSpline<Vector2>, base: Color, end: Color){

       sr.renderer.apply {

            begin(sr.projectionMatrix, GL20.GL_TRIANGLE_FAN)
            color(base.r, base.g, base.b, base.a)
            vertex(basePoint.x, basePoint.y, 0.0f)

            //draw a fan from this point
            var value = 0f
            while (value <= 1f) {
                color(end.r, end.g, end.b, end.a)
                spline.valueAt(/* out: */tmpV, value)
                vertex(tmpV.x, tmpV.y, 0.0f)
                value += SAMPLE_POINT_DISTANCE
            }
            end()
        }
    }

    val konstant  = DaySystem.SECONDS_PER_DAY/2.0f

    private fun getPercentage(time:Float):Float{
        return time.toFloat() / konstant.toFloat()
    }

}



