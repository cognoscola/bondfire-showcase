package com.bondfire.app.background.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.utils.TimeUtils

import com.bondfire.app.background.components.CarcadianComponent
import com.bondfire.app.background.components.ColorComponent
import com.bondfire.app.background.components.PositionComponent

/**
 * Created by alvaregd on 2016-09-11.
 * Determines the time of the day and base of objects due to the base of the day
 */
class DaySystem: IntervalIteratingSystem {

    lateinit var carcadianComponentMapper: ComponentMapper<CarcadianComponent>
    lateinit var positionComponentMapper: ComponentMapper<PositionComponent>
    lateinit var colorComponentMapper: ComponentMapper<ColorComponent>

//    constructor() : super(Family.all(CarcadianComponent::class.java).get(), 1.0f) {
    constructor() : super(Family.all(CarcadianComponent::class.java).get(), 0.025f) {

        carcadianComponentMapper = ComponentMapper.getFor(CarcadianComponent::class.java)
        positionComponentMapper = ComponentMapper.getFor(PositionComponent::class.java)
        colorComponentMapper = ComponentMapper.getFor(ColorComponent::class.java)
    }

    override fun processEntity(entity: Entity?) {

        carcadianComponentMapper.get(entity).apply {
            function?.invoke()
        }
    }

    companion object {

//                val SECONDS_PER_DAY = 86400
        val SECONDS_PER_DAY = 1200

        fun getTime(): Long {

//            return 0.toLong()
            return (TimeUtils.nanoTime() / 1E9).toLong()
        }
    }
}