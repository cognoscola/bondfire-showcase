package com.bondfire.app.background.components

import com.badlogic.ashley.core.Component

/**
 * Created by alvaregd on 2016-09-15.
 */
class CarcadianComponent : Component {

    var age:Long = 0
    var function: (() -> Unit)? = null
}