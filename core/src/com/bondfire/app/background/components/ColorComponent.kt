package com.bondfire.app.background.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color

/**
 * Created by alvaregd on 2016-09-17.
 */
class ColorComponent:Component{

    val skyColorLeft = Color()
    val floorColorLeft = Color()

    val skyColorRight = Color()
    val floorColorRight = Color()

    companion object{
        val CHARMAX = 255f
    }

}