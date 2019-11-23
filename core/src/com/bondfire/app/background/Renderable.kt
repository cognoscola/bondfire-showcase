package com.bondfire.app.background

import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * Created by alvaregd on 2016-09-17.
 */
interface Renderable {

    fun render(renderer: ShapeRenderer, dt:Float)
}