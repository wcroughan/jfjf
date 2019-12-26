package com.example.demo.view

import com.example.demo.controller.DotController
import javafx.animation.AnimationTimer
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*
import java.util.concurrent.ThreadLocalRandom
import javax.sound.sampled.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.math.abs
import kotlin.math.sign

val minDotRadius = 2.0
val maxDotRadius = 5.0
val maxDotCoord = 400
class MainView : View("Hello TornadoFX") {
//    val rand = Random(420)
    val controller: DotController by inject()

    var dots = emptyList<Circle>()

    val animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            dots.forEachIndexed { index, circle ->
                val d = controller.allDots[index]
                val xx = if (d.x < -1.0) -1.0 else if (d.x > 1.0) 1.0 else d.x
                val yy = if (d.y < -1.0) -1.0 else if (d.y > 1.0) 1.0 else d.y
                circle.centerX = xx  * maxDotCoord
                circle.centerY = yy * maxDotCoord
                circle.fill = d.fillColor
                circle.radius = d.radius
            }
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

            dots = List(controller.numDots) { circle {
                fill = Color.ORANGE
                radius = maxDotRadius
            }}

            onMouseClicked = EventHandler<MouseEvent> {
                controller.isRunning = !controller.isRunning
            }
        }
    }

    init {
        animationTimer.start()
//        controller.isRunning = true
    }

}