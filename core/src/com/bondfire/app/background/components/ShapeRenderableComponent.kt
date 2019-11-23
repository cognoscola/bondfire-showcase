package com.bondfire.app.background.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.bondfire.app.background.Renderable

/**
 * Created by alvaregd on 2016-09-17.
 */
class ShapeRenderableComponent:Component{
    var render:((renderer:ShapeRenderer, dt:Float) -> Unit)? = null
}