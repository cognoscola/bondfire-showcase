package com.bondfire.app.background.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.bondfire.app.background.components.BatchRenderableComponent
import com.bondfire.app.background.components.PositionComponent
import com.bondfire.app.background.components.ShapeRenderableComponent

/**
 * Created by alvaregd on 2016-12-10.
 */
class BatchRenderSystem: IteratingSystem {

    private lateinit var batchRenderableMapper: ComponentMapper<BatchRenderableComponent>
    private lateinit var batch:SpriteBatch

    constructor():super(Family.all(
            BatchRenderableComponent::class.java,
            PositionComponent::class.java).get()){

        batchRenderableMapper = ComponentMapper.getFor(BatchRenderableComponent::class.java)
        batch = SpriteBatch()
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {

        val batchComponent = batchRenderableMapper.get(entity)
        batchComponent.render?.invoke(batch, deltaTime)
    }

    fun dispose(){
        batch.dispose()
    }

}