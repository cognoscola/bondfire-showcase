package com.bondfire.app.background

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

/**
 * Created by alvaregd on 2016-10-16.
 */
class ColorConstant {
    companion object{


        /**
         * sRGB to XYZ conversion matrix
         */
        var M = arrayOf(doubleArrayOf(0.4124, 0.3576, 0.1805), doubleArrayOf(0.2126, 0.7152, 0.0722), doubleArrayOf(0.0193, 0.1192, 0.9505))

        /**
         * XYZ to sRGB conversion matrix
         */
        val Mi = arrayOf(doubleArrayOf(3.2406, -1.5372, -0.4986), doubleArrayOf(-0.9689, 1.8758, 0.0415), doubleArrayOf(0.0557, -0.2040, 1.0570))

        var chromaD65 = doubleArrayOf(0.3127, 0.3290, 100.0)
        var chromaD75 = doubleArrayOf(0.2990, 0.3149, 100.0)
        var chromaWhitePoint = chromaD65


        /**
         * Convert XYZ to RGB.
         * @param X
         * *
         * @param Y
         * *
         * @param Z
         * *
         * @return RGB in int array.
         */
        fun XYZtoRGB(X: Double, Y: Double, Z: Double): IntArray {
            val result = IntArray(3)

            val x = X / 100.0
            val y = Y / 100.0
            val z = Z / 100.0

            // [r g b] = [x y Z][ColorConstant.Mi]
            var r = x * Mi[0][0] + y * Mi[0][1] + z * Mi[0][2]
            var g = x * Mi[1][0] + y * Mi[1][1] + z * Mi[1][2]
            var b = x * Mi[2][0] + y * Mi[2][1] + z * Mi[2][2]

            // assume sRGB
            if (r > 0.0031308) {
                r = 1.055 * Math.pow(r, 1.0 / 2.4) - 0.055
            } else {
                r = r * 12.92
            }
            if (g > 0.0031308) {
                g = 1.055 * Math.pow(g, 1.0 / 2.4) - 0.055
            } else {
                g = g * 12.92
            }
            if (b > 0.0031308) {
                b = 1.055 * Math.pow(b, 1.0 / 2.4) - 0.055
            } else {
                b = b * 12.92
            }

            r = if (r < 0) 0.toDouble() else r
            g = if (g < 0) 0.toDouble() else g
            b = if (b < 0) 0.toDouble() else b

            // convert 0..1 into 0..255
            result[0] = Math.round(r * 255).toInt()
            result[1] = Math.round(g * 255).toInt()
            result[2] = Math.round(b * 255).toInt()

            return result
        }


        /**
         * @param X the x compoment
         * *
         * @param Y the y compoment
         * *
         * @param Z the Z component
         * *
         * @return xyY values
         */
        fun XYZtoxyY(X: Double, Y: Double, Z: Double): DoubleArray {
            val result = DoubleArray(3)
            if (X + Y + Z == 0.0) {
                result[0] = ColorConstant.chromaWhitePoint[0]
                result[1] = ColorConstant.chromaWhitePoint[1]
                result[2] = ColorConstant.chromaWhitePoint[2]
            } else {
                result[0] = X / (X + Y + Z)
                result[1] = Y / (X + Y + Z)
                result[2] = Y
            }
            return result
        }

        fun xyY2XYZ(x: Float, y: Float, Y: Float): Vector3 {

            val X1: Float
            val Y1: Float
            val Z1: Float

            if (y == 0f) {
                X1 = 0f
                Y1 = 0f
                Z1 = 0f
            } else {
                X1 = x * Y / y
                Y1 = Y
                Z1 = (1f - x - y) * Y / y
            }

            return Vector3(X1, Y1, Z1)
        }

        /**
         * Convert RGB to XYZ
         * @return XYZ in double array.
         */
        fun RGB2XYZ(R:Float, G:Float,B:Float):DoubleArray {
            val result = DoubleArray(3)

            // convert 0..255 into 0..1
            var r = R / 255.0
            var g = G / 255.0
            var b = B / 255.0

            // assume sRGB
            if (r <= 0.04045) {
                r = r / 12.92
            } else {
                r = Math.pow((r + 0.055) / 1.055, 2.4)
            }
            if (g <= 0.04045) {
                g = g / 12.92
            } else {
                g = Math.pow((g + 0.055) / 1.055, 2.4)
            }
            if (b <= 0.04045) {
                b = b / 12.92
            } else {
                b = Math.pow((b + 0.055) / 1.055, 2.4)
            }

            r *= 100.0
            g *= 100.0
            b *= 100.0

            // [X Y Z] = [r g b][M]

            result[0] = r * ColorConstant.M[0][0] + g * ColorConstant.M[0][1] + b * ColorConstant.M[0][2]
            result[1] = r * ColorConstant.M[1][0] + g * ColorConstant.M[1][1] + b * ColorConstant.M[1][2]
            result[2] = r * ColorConstant.M[2][0] + g * ColorConstant.M[2][1] + b * ColorConstant.M[2][2]

            return result
        }


    }
}

fun Color.toxyY():DoubleArray{

    val XZY = ColorConstant.RGB2XYZ(this.r,this.g,this.b)
    return  ColorConstant.XYZtoxyY(XZY[0],XZY[1],XZY[2])

}

fun Color.toRGB(xyY:DoubleArray){

    val XYZ = ColorConstant.xyY2XYZ(xyY[0].toFloat(),xyY[1].toFloat(),xyY[2].toFloat())
    val rgb = ColorConstant.XYZtoRGB(XYZ.x.toDouble(), XYZ.y.toDouble(), XYZ.z.toDouble())
    this.r = rgb[0] / 255.0f
    this.g = rgb[1] / 255.0f
    this.b = rgb[2] / 255.0f
}


