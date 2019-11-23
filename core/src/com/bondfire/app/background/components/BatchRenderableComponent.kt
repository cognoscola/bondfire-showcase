package com.bondfire.app.background.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Created by alvaregd on 2016-12-10.
 */
class BatchRenderableComponent: Component {
    var render:((renderer: SpriteBatch, dt:Float) -> Unit)? = null
}