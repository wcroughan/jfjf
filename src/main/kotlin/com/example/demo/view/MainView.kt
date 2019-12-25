package com.example.demo.view

import javafx.animation.AnimationTimer
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.concurrent.timer
import kotlin.math.abs

val numDots = 2000
val dotRadius = 15.0
val dotOpacity = 0.5
val maxDotCoord = 400
val dotPositionCutoff = 1.0
//val myrand = Random(421)
val myrand = ThreadLocalRandom.current()

val physicsFrameRate = 30.0
val physicsSpeedFactor = 0.1
val deltaT = physicsSpeedFactor / physicsFrameRate
val animationFrameRate = 20.0
val velFactor = 0.05
val centerPullFactor = 0.2

class MainView : View("Hello TornadoFX") {
//    val rand = Random(420)
    var isRunning = false

    class dot {
        var x = myrand.nextDouble(-0.3,0.3)
        var y = myrand.nextDouble(-0.3,0.3)
        var velx = myrand.nextDouble()
        var vely = myrand.nextDouble()
        var gravity = myrand.nextDouble(-1.0, 1.0)
        var spin = myrand.nextDouble(-1.0, 1.0)
    }

    var allDots = Array(numDots) { dot() }

    fun updateDots() {
        allDots.forEach { dot1 ->
//            dot1.vely = myrand.nextDouble(-1.0,1.0) - dot1.y
//            dot1.velx = myrand.nextDouble(-1.0,1.0) - dot1.x

            allDots.forEach { dot2 ->
                if (dot1.x != dot2.x || dot1.y != dot2.y) {
                    val gfactor = (dot2.gravity + dot1.gravity) /
                            Math.sqrt(Math.pow(dot1.x - dot2.x, 2.0) + Math.pow(dot1.y - dot2.y, 2.0)) *
                            0.01 / numDots * 1000.0
                    dot1.velx += (dot2.x - dot1.x + (dot2.y - dot1.y) * dot1.spin * 0.05) * gfactor
                    dot1.vely += (dot2.y - dot1.y - (dot2.x - dot1.x) * dot1.spin * 0.05) * gfactor
                }
            }
        }

        val mx = allDots.sumByDouble { it.x } / numDots.toDouble()
        val my = allDots.sumByDouble { it.y } / numDots.toDouble()

        allDots.forEach {
            it.x += it.velx * deltaT * velFactor - mx
            if (it.x < -dotPositionCutoff) it.x = -dotPositionCutoff else if (it.x > dotPositionCutoff) it.x = dotPositionCutoff
            it.y += it.vely * deltaT * velFactor - my
            if (it.y < -dotPositionCutoff) it.y = -dotPositionCutoff else if (it.y > dotPositionCutoff) it.y = dotPositionCutoff

            it.velx -= it.x * centerPullFactor
            it.vely -= it.y * centerPullFactor

//            it.gravity = -1.0 + (abs(it.x) + abs(it.y)) / 2.0
            it.gravity = -1.0 + (abs(it.x) + abs(it.y)) * 2.0
        }
    }

    var dotMoveTimer = timer(daemon = true, initialDelay = 1500, period = (1000.0/ physicsFrameRate).toLong()) {
        if (isRunning)
            updateDots()
    }

    var dots = emptyList<Circle>()

    val timer = object : AnimationTimer() {
        override fun handle(now: Long) {
            dots.forEachIndexed { index, circle ->
                val xx = if (allDots[index].x < -1.0) -1.0 else if (allDots[index].x > 1.0) 1.0 else allDots[index].x
                val yy = if (allDots[index].y < -1.0) -1.0 else if (allDots[index].y > 1.0) 1.0 else allDots[index].y
                circle.centerX = xx  * maxDotCoord
                circle.centerY = yy * maxDotCoord
                val gfil = (allDots[index].gravity + 1.0) / 4.0
                circle.fill = Color.color(if (gfil < 0.0) 0.0 else if (gfil > 1.0) 1.0 else gfil, allDots[index].spin / 2.0 + 0.5, 0.0, dotOpacity)
//                circle.fill = Color.color(allDots[index].gravity / 2.0 + 0.5, allDots[index].spin / 2.0 + 0.5, 0.0)
//                circle.fill = Color.hsb(allDots[index].gravity / 2.0 + 0.5, 1.0, allDots[index].spin / 2.0 + 0.5)
            }
        }
    }

    override val root = stackpane {
        group {
            padding = Insets(20.0)

            rectangle {
                fill = Color.BLUE
                x = -maxDotCoord.toDouble() - dotRadius
                y = -maxDotCoord.toDouble() - dotRadius
                width = 2.0*maxDotCoord.toDouble() + 2.0 * dotRadius
                height = 2.0*maxDotCoord.toDouble() + 2.0 * dotRadius
            }

            dots = List(numDots) { circle {
                fill = Color.ORANGE
                radius = dotRadius
            }}

            onMouseClicked = EventHandler<MouseEvent> {
                isRunning = !isRunning
            }
        }
    }

    init {
        timer.start()
//        isRunning = true
    }

}