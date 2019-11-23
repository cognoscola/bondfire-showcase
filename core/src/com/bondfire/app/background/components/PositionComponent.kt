package com.bondfire.app.background.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

/**
 * Created by alvaregd on 2016-09-16.
 */
class PositionComponent:Component{

    val position = Vector2(0.0f,0.0f)
    var positionFunction: (() -> Any)? = null
}
