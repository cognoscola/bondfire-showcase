package com.bondfire.app.background


import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.bondfire.app.background.components.*
import com.bondfire.app.background.systems.DaySystem
import com.bondfire.app.background.systems.ShapeRenderSystem

class GradientBoardV2:Entity {
    val TAG: String = GradientBoardV2::class.java.name

    private var x: Float = 0.0f
    private var y: Float = 0.0f
    private var Width: Int = 0
    private var Height: Int = 0

    //color format is #RRGGBBAA
    private val color1CeilingLeft = Color.valueOf("#200D22")//12am
    private val color2CeilingLeft = Color.valueOf("#1F2334")//3am
    private val color3CeilingLeft = Color.valueOf("#4774DA")//6am
    private val color4CeilingLeft = Color.valueOf("#2974DA")//9am
    private val color5CeilingLeft = Color.valueOf("#0F91FF")//12noon
    private val color6CeilingLeft = Color.valueOf("#85A7DA")//3pm
    private val color7CeilingLeft = Color.valueOf("#A64D1C")//6pm E7591C
    private val color8CeilingLeft = Color.valueOf("#2D50DE")//9pm

    private val color1CeilingRight = Color.valueOf("#200D22")//12am
    private val color2CeilingRight = Color.valueOf("#1F2334")//3am
    private val color3CeilingRight = Color.valueOf("#93C9DA")//6am
    private val color4CeilingRight = Color.valueOf("#85A7DA")//9am
    private val color5CeilingRight = Color.valueOf("#0F91FF")//12noon
    private val color6CeilingRight = Color.valueOf("#2974DA")//3pm
    private val color7CeilingRight = Color.valueOf("#2D50DE")//6pm
    private val color8CeilingRight = Color.valueOf("#2438B0")//9pm

    private val color1FloorLeft = Color.valueOf("#200D22")//12
    private val color2FloorLeft = Color.valueOf("#1F2334")//3am
    private val color3FloorLeft = Color.valueOf("#4141BB")//6am
    private val color4FloorLeft = Color.valueOf("#0F91FF")//9am
    private val color5FloorLeft = Color.valueOf("#71DEBA")//12noon
    private val color6FloorLeft = Color.valueOf("#654EDE")//3pm
    private val color7FloorLeft = Color.valueOf("#FF814E")//6pm
    private val color8FloorLeft = Color.valueOf("#2B2E64")//9pm

    private val color1FloorRight = Color.valueOf("#200D22")//12
    private val color2FloorRight = Color.valueOf("#1F2334")//3am
    private val color3FloorRight = Color.valueOf("#B0C5DA")//6am B0C5DA
    private val color4FloorRight = Color.valueOf("#0F91FF")//9am
    private val color5FloorRight = Color.valueOf("#71DEBA")//12noon
    private val color6FloorRight = Color.valueOf("#654EDE")//3pm
    private val color7FloorRight = Color.valueOf("#F17B34")//6pm
    private val color8FloorRight = Color.valueOf("#2B2E64")//9pm

    private val colorCeilingLeftInitial = Color()
    private val colorCeilingLeftFinal = Color()

    private val colorCeilingRightInitial = Color()
    private val colorCeilingRightFinal = Color()

    private val colorFloorLeftInitial = Color()
    private val colorFloorLeftFinal = Color()

    private val colorFloorRightInitial = Color()
    private val colorFloorRightFinal = Color()

    private var seconds = 0L
    private var percentage = 0.0f

    val positionComponent = PositionComponent()

    lateinit var font:BitmapFont

    constructor() {

        font = BitmapFont()
        font.setColor(Color.RED)

        Width = ShapeRenderSystem.WORLD_WIDTH
        Height = ShapeRenderSystem.WINDOW_HEIGHT
        this.x = 0.0f
        this.y = 0.0f

        add(ColorComponent())
        add(CarcadianComponent().apply {

            age = DaySystem.getTime()
            calculateColor(age)
            function = {
                age++
                calculateColor(age)
            }
        })

        add(positionComponent)

        add(ShapeRenderableComponent().apply {
            render = { sr, dt ->

                val colorComponent = getComponent(ColorComponent::class.java)
                if (colorComponent != null) {

                    sr.begin(ShapeRenderer.ShapeType.Filled)
                    sr.rect(
                            positionComponent.position.x,
                            positionComponent.position.y,
                            Width.toFloat(), Height.toFloat(),
                            colorComponent.floorColorLeft, colorComponent.floorColorRight, colorComponent.skyColorRight, colorComponent.skyColorLeft)
                    sr.end()
                }
            }
        })

/*
        add(BatchRenderableComponent().apply {
            render ={ batch, dt ->

                batch.begin()
                font.draw(batch, "Seconds: ${seconds}", 10.0f, 20.0f)
                font.draw(batch, "Percent: $percentage",10.0f, 40.0f)

                batch.end()
            }
        })
*/
    }

    private fun calculateColor(age:Long) {
        val colorComponent = getComponent(ColorComponent::class.java)

        seconds = age % DaySystem.SECONDS_PER_DAY

        if (seconds >= (DaySystem.SECONDS_PER_DAY * (7.0/8.0))) {


            //Left
            colorCeilingLeftFinal.set(color1CeilingLeft)
            colorCeilingLeftInitial.set(color8CeilingLeft)

            colorFloorLeftFinal.set(color1FloorLeft)
            colorFloorLeftInitial.set(color8FloorLeft)

            //right
            colorFloorRightFinal.set(color1FloorRight)
            colorFloorRightInitial.set(color8FloorRight)

            colorCeilingRightFinal.set(color1CeilingRight)
            colorCeilingRightInitial.set(color8CeilingRight)

        } else if (seconds >= (DaySystem.SECONDS_PER_DAY * (6.0/8.0))) {

            colorCeilingLeftFinal.set(color8CeilingLeft)
            colorCeilingLeftInitial.set(color7CeilingLeft)

            colorFloorLeftFinal.set(color8FloorLeft)
            colorFloorLeftInitial.set(color7FloorLeft)

            colorFloorRightFinal.set(color8FloorRight)
            colorFloorRightInitial.set(color7FloorRight)

            colorCeilingRightFinal.set(color8CeilingRight)
            colorCeilingRightInitial.set(color7CeilingRight)

        } else if (seconds >= (DaySystem.SECONDS_PER_DAY * (5.0/8.0))) {


            colorCeilingLeftFinal.set(color7CeilingLeft)
            colorCeilingLeftInitial.set(color6CeilingLeft)

            colorFloorLeftFinal.set(color7FloorLeft)
            colorFloorLeftInitial.set(color6FloorLeft)

            colorFloorRightFinal.set(color7FloorRight)
            colorFloorRightInitial.set(color6FloorRight)

            colorCeilingRightFinal.set(color7CeilingRight)
            colorCeilingRightInitial.set(color6CeilingRight)

        } else if (seconds >= (DaySystem.SECONDS_PER_DAY * (4.0/8.0))) {


            colorCeilingLeftFinal.set(color6CeilingLeft)
            colorCeilingLeftInitial.set(color5CeilingLeft)

            colorFloorLeftFinal.set(color6FloorLeft)
            colorFloorLeftInitial.set(color5FloorLeft)

            colorFloorRightFinal.set(color6FloorRight)
            colorFloorRightInitial.set(color5FloorRight)

            colorCeilingRightFinal.set(color6CeilingRight)
            colorCeilingRightInitial.set(color5CeilingRight)

        } else if (seconds >= (DaySystem.SECONDS_PER_DAY * (3.0/8.0))) {


            colorCeilingLeftFinal.set(color5CeilingLeft)
            colorCeilingLeftInitial.set(color4CeilingLeft)

            colorFloorLeftFinal.set(color5FloorLeft)
            colorFloorLeftInitial.set(color4FloorLeft)

            colorFloorRightFinal.set(color5FloorRight)
            colorFloorRightInitial.set(color4FloorRight)

            colorCeilingRightFinal.set(color5CeilingRight)
            colorCeilingRightInitial.set(color4CeilingRight)

        } else if (seconds >= (DaySystem.SECONDS_PER_DAY * (2.0/8.0))) {


            colorCeilingLeftFinal.set(color4CeilingLeft)
            colorCeilingLeftInitial.set(color3CeilingLeft)

            colorFloorLeftFinal.set(color4FloorLeft)
            colorFloorLeftInitial.set(color3FloorLeft)

            colorFloorRightFinal.set(color4FloorRight)
            colorFloorRightInitial.set(color3FloorRight)

            colorCeilingRightFinal.set(color4CeilingRight)
            colorCeilingRightInitial.set(color3CeilingRight)

        } else if (seconds >= (DaySystem.SECONDS_PER_DAY * (1.0/8.0))) {


            colorCeilingLeftFinal.set(color3CeilingLeft)
            colorCeilingLeftInitial.set(color2CeilingLeft)

            colorFloorLeftFinal.set(color3FloorLeft)
            colorFloorLeftInitial.set(color2FloorLeft)

            colorFloorRightFinal.set(color3FloorRight)
            colorFloorRightInitial.set(color2FloorRight)

            colorCeilingRightFinal.set(color3CeilingRight)
            colorCeilingRightInitial.set(color2CeilingRight)
        } else {

            
            colorCeilingLeftFinal.set(color2CeilingLeft)
            colorCeilingLeftInitial.set(color1CeilingLeft)

            colorFloorLeftFinal.set(color2FloorLeft)
            colorFloorLeftInitial.set(color1FloorLeft)

            colorFloorRightFinal.set(color2FloorRight)
            colorFloorRightInitial.set(color1FloorRight)

            colorCeilingRightFinal.set(color2CeilingRight)
            colorCeilingRightInitial.set(color1CeilingRight)
        }

        percentage = this.getPercentage(seconds % (konstant))
        percentage = MathUtils.clamp(percentage,0.0f,1.0f)

        if (colorComponent != null) {

            colorComponent.skyColorLeft.set(colorCeilingLeftInitial.lerp(colorCeilingLeftFinal,percentage))
            colorComponent.skyColorRight.set(colorCeilingRightInitial.lerp(colorCeilingRightFinal,percentage))

            colorComponent.floorColorLeft.set(colorFloorLeftInitial.lerp(colorFloorLeftFinal,percentage))
            colorComponent.floorColorRight.set(colorFloorRightInitial.lerp(colorFloorRightFinal,percentage))
        }else{
            Gdx.app.log(TAG,"calculateColor Null color! ")
        }
    }


    val konstant  = DaySystem.SECONDS_PER_DAY/8.0f


    private fun getPercentage(time:Float):Float{
        return time.toFloat() / konstant.toFloat()
    }
}





