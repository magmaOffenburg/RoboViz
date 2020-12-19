package org.magmaoffenburg.roboviz.util

import java.awt.*


object SwingUtils {

    private fun getCurrentScreen(location: Point, size: Dimension): GraphicsDevice {
        val devices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        var bestMatch = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        var bestPercentage = 0f

        devices.forEach { device ->
            val bounds: Rectangle = device.defaultConfiguration.bounds
            val percentage: Float = getPercentageOnScreen(location, size, bounds)
            if (percentage > bestPercentage) {
                bestMatch = device
                bestPercentage = percentage
            }
        }

        return bestMatch
    }

    private fun getPercentageOnScreen(location: Point, size: Dimension, screen: Rectangle): Float {
        val intersection = Rectangle(location, size).createIntersection(screen)
        val frameArea = size.width * size.height
        val intersectionArea = (intersection.width * intersection.height).toInt()
        val percentage = intersectionArea.toFloat() / frameArea
        return if (percentage < 0) 0f else percentage
    }

    fun centerWindowOnScreen(window: Window, desiredLocation: Point): Point {
        val currentScreen = getCurrentScreen(desiredLocation, window.size)
        val screenBounds = currentScreen.defaultConfiguration.bounds

        return Point(
                screenBounds.centerX.toInt() - (window.width / 2),
                screenBounds.centerY.toInt() - (window.height / 2)
        )
    }

}
