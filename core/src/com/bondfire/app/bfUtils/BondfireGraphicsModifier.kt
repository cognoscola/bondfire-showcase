package com.bondfire.app.bfUtils

/**
 * Created by alvaregd on 14/05/16.
 */
interface BondfireGraphicsModifier {

    /**
     * When the bondfire platform changes state (I.e, the user decides to view other areas of the
     * app, while a game is running  the graphics within the game need to change so that
     * Platform UI stands out into the foreground. The game may decide by itself how it should change
     * @param effectPerfect how much will the graphics by modified?
     */
    fun setGraphicsEffectPercent(effectPerfect: Float)

    /**
     * bondfire has 3 visible pages. Let the container know which page we are viewing
     * @param page
     */

    fun onPageScrolled(position: Int, positionOffset:Float, positionOffsetPixels:Int)
}
