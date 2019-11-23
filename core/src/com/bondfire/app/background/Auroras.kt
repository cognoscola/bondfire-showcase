package com.bondfire.app.background

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.bondfire.app.background.components.DimensionComponent
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent

/**
 * Created by alvaregd on 2016-10-27.
 */

class Auroras: Entity{

    val auroraColor = Color(1.0f,1.0f,1.0f,0.5f)

    constructor(){

        add(PositionComponent().apply {
            position.x = 0.0f
            position.y = Mountains.mountainBaseHeight * Gdx.graphics.height

        })

        add(DimensionComponent().apply {
            dimensions.x = Gdx.graphics.width.toFloat()
            dimensions.y = (1 - Mountains.mountainBaseHeight )* Gdx.graphics.height
        })

        add(ShapeRenderableComponent().apply {

            render = { sr, dt ->

                val position = getComponent(PositionComponent::class.java)
                val dimension = getComponent(DimensionComponent::class.java)

                sr.apply {

                    Gdx.gl.glEnable(GL20.GL_BLEND)
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                    begin(ShapeRenderer.ShapeType.Filled)
                    rect(position.position.x, position.position.y, dimension.dimensions.x, dimension.dimensions.y,auroraColor,auroraColor,auroraColor,auroraColor)
                    end()
                    Gdx.gl.glDisable(GL20.GL_BLEND)

                    renderer.apply {

                        begin(projectionMatrix, GL20.GL_LINES)
                        color(1.0f, 0.0f, 0.0f, 0.0f)
                        vertex(Gdx.graphics.width * 0.3f, Gdx.graphics.height * 0.75f, 0.0f)
                        color(1.0f, 0.0f, 0.0f, 0.0f)
                        vertex(Gdx.graphics.width * 0.75f, Gdx.graphics.height * 0.75f, 0.0f)

                        color(1.0f, 0.0f, 0.0f, 0.0f)
                        vertex(Gdx.graphics.width * 0.3f, Gdx.graphics.height * 0.70f, 0.0f)
                        color(1.0f, 0.0f, 0.0f, 0.0f)
                        vertex(Gdx.graphics.width * 0.75f, Gdx.graphics.height * 0.70f, 0.0f)

                        end()
                    }

                }
            }
        })

    }
}
