package com.example.demo.view

import com.example.demo.controller.Dot
import com.example.demo.controller.DotController
import javafx.animation.AnimationTimer
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import tornadofx.*
import java.util.concurrent.ThreadLocalRandom
import javax.sound.sampled.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.math.abs
import kotlin.math.sign

val minDotRadius = 4.0
val maxDotRadius = 9.5
val maxDotCoord = 400
val angleRefreshRate = 30.0
val angleRPS = 0.05
val deltaAngle = angleRPS / angleRefreshRate * Math.PI * 2.0

class MainView : View("Hello TornadoFX") {
//    val rand = Random(420)
    val controller: DotController by inject()

//    var dots = emptyList<Circle>()
    var lines = emptyList<Triple<Line, Dot, Dot>>()
    var angle = 0.0
    var isRunning = false

    val angleTimer = timer(null, true, 0, (1000.0 / angleRefreshRate).toLong()) {
        if (isRunning)
            angle = (angle + deltaAngle).rem(2.0*Math.PI)
    }

    val animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            lines.forEach {
                it.first.apply {
                    val d = it.second
                    val x1 = if (d.x < -1.0) -1.0 else if (d.x > 1.0) 1.0 else d.x
                    val y1 = if (d.y < -1.0) -1.0 else if (d.y > 1.0) 1.0 else d.y
                    val z1 = if (d.z < -1.0) -1.0 else if (d.z > 1.0) 1.0 else d.z

                    val d2 = it.third
                    val x2 = if (d2.x < -1.0) -1.0 else if (d2.x > 1.0) 1.0 else d2.x
                    val y2 = if (d2.y < -1.0) -1.0 else if (d2.y > 1.0) 1.0 else d2.y
                    val z2 = if (d2.z < -1.0) -1.0 else if (d2.z > 1.0) 1.0 else d2.z

                    val vx1 = (x1 * Math.sin(angle) + z1 * Math.cos(angle)) / 1.36
                    val vx2 = (x2 * Math.sin(angle) + z2 * Math.cos(angle)) / 1.36

                    startX = vx1 * maxDotCoord
                    startY = y1 * maxDotCoord
                    endX = vx2 * maxDotCoord
                    endY = y2 * maxDotCoord
                    stroke = d.fillColor
//                    println("${d.fillColor.brightness}")

//                    fill = it.second.fillColor
//                    fill = Color.WHITE
//                    println("$startX, $startY, $endX, $endY")
                }
            }


//            dots.forEachIndexed { index, circle ->
//                val d = controller.allDots[index]
//                val xx = if (d.x < -1.0) -1.0 else if (d.x > 1.0) 1.0 else d.x
//                val yy = if (d.y < -1.0) -1.0 else if (d.y > 1.0) 1.0 else d.y
//                val zz = if (d.z < -1.0) -1.0 else if (d.z > 1.0) 1.0 else d.z
//
//                circle.centerX = (xx * Math.sin(angle) + zz * Math.cos(angle)) * maxDotCoord / 1.36
//                circle.centerY = yy * maxDotCoord
//                circle.fill = d.fillColor
//                circle.radius = d.radius
//            }
        }
    }

    override val root = stackpane {
        group {
            padding = Insets(20.0)

            rectangle {
                fill = Color.BLACK
                x = -maxDotCoord.toDouble() - maxDotRadius
                y = -maxDotCoord.toDouble() - maxDotRadius
                width = 2.0*maxDotCoord.toDouble() + 2.0 * maxDotRadius
                height = 2.0*maxDotCoord.toDouble() + 2.0 * maxDotRadius
            }

//            dots = List(controller.numDots) { circle {
//                fill = Color.ORANGE
//                radius = maxDotRadius
//            }}

            val startLines = List(controller.numLines) { line {
                startY = maxDotCoord.toDouble()
                startX = maxDotCoord.toDouble()
                endY = 0.0
                endX = 0.0
//                strokeWidth = 20.0
//                fill = Color.WHITE
            }}

            lines = controller.allDots.flatMap { dot ->
                dot.outEdges.map { d2 ->
                    Pair(dot, controller.allDots[d2])
                }
            }.mapIndexed {index, pair ->
                Triple(startLines[index], pair.first, pair.second)
            }

            onMouseClicked = EventHandler<MouseEvent> {
                controller.playPause()
                isRunning = !isRunning
//                println("$isRunning")
            }
        }
    }

    init {
        animationTimer.start()
//        controller.isRunning = true
    }

}