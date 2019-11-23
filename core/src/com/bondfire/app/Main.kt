package com.bondfire.app

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.utils.ScreenUtils
import com.bondfire.app.background.ShevchenkoWallpaper
import com.bondfire.app.background.systems.ShapeRenderSystem
import com.bondfire.app.bfUtils.BondfireGraphicsModifier


class Main() : ApplicationAdapter(), BondfireGraphicsModifier {
    val TAG: String = Main::class.java.name

    /** We will use this object to draw rectangle  */
    private  lateinit var background: ShevchenkoWallpaper
    private var ashley:Engine? = null

    override fun create() {
        ashley = PooledEngine()
        background = ShevchenkoWallpaper(ashley)
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        ashley?.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()
        background.dispose()
    }

    interface PixmapListener {
        fun PixmapGenerated(pixmap: Pixmap)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        ashley?.getSystem(ShapeRenderSystem::class.java)?.viewport?.update(width,height)
    }

    override fun setGraphicsEffectPercent(effectPerfect: Float) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        ashley?.getSystem(ShapeRenderSystem::class.java)?.calculateWindowPosition(position,positionOffset)
    }

    companion object {

        fun getScreenshot(width: Int, height: Int, yDown: Boolean, listener: PixmapListener) {
            println("getScreenshot()")

            Thread(Runnable {
                Gdx.app.postRunnable {
                    val background = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.width, Gdx.graphics.height)
                    println("Done getting framebufer")
                    if (yDown) {
                        // Flip the pixmap upside down
                        val pixels = background.pixels
                        val numBytes = width * height * 4
                        val lines = ByteArray(numBytes)
                        val numBytesPerLine = width * 4
                        for (i in 0..height -   1) {
                            pixels.position((height - i - 1) * numBytesPerLine)
                            pixels.get(lines, i * numBytesPerLine, numBytesPerLine)
                        }
                        pixels.clear()
                        pixels.put(lines)
                    }
                    listener.PixmapGenerated(background)
                    println("Callback called")
                }
            }).start()
        }
    }



}
