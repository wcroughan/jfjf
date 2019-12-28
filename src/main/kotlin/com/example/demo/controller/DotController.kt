package com.example.demo.controller

import com.example.demo.view.maxDotRadius
import com.example.demo.view.minDotRadius
import javafx.scene.paint.Color
import tornadofx.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.concurrent.timer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

val dotPositionCutoff = 1.0
val wallBounceFactor = 0.25
val dotInitialSpread = 0.98
//val dotInitialSpread = 0.68
//val myrand = Random(421)
val myrand = ThreadLocalRandom.current()
var cvFactor = 0.2
var radFactor = 4000.0
val spinValChangeSpeed = 0.00002
//val spinValChangeSpeed = 0.0
val homeDragSpeed = 0.0

val physicsFrameRate = 30.0
val physicsSpeedFactor = 0.03
var deltaT = physicsSpeedFactor / physicsFrameRate
val deltaDeltaT = deltaT / 100.0 / physicsFrameRate
val velFactor = 0.05
val pullFactor = 3.4
val spinFactor = 10.0
val centerPullFactor = 200.2
//val centerPullFactor = 0.0
val frictionFactor = 0.99
val pushFactor = 0.6
val pushValFadeRate = 0.75
val pushValJump = 100.0
val pushJumpProb = 0.99
val pullValFadeRate = 0.85
val pullValJump = 400.0
val pullJumpProb = 0.995
//val superPullJumpProb = 0.9995
//val superPullJumpVal = 5000.0

var targetAvgVel = 500.0
var velCorrectionFactor = 1.0
var velCorrectionFactorSpeed = 0.02

var moveHomeRate = 0.25

class DotController : Controller() {
//    val numDots = 1936
    val numDots = 100
    val audioColorMap = AudioColorMap()

    var isRunning = false
    //    var allDots = Array(numDots) { Dot() }
    var allDots = (0 until numDots).map { i ->
//        val ndsr = Math.floor(Math.sqrt(numDots.toDouble())).toInt()
//        Dot((-1 + 2.0*(i.rem(ndsr).toDouble() + 0.5) / ndsr.toDouble()) * dotInitialSpread,
//                (-1 + 2.0*(Math.floorDiv(i, ndsr).toDouble() + 0.5) / ndsr.toDouble())* dotInitialSpread)
        val polarAngle = myrand.nextDouble(0.0, Math.PI)
//        val polarAngle = myrand.nextDouble(-Math.PI, Math.PI)
        val azimuthAngle = myrand.nextDouble(0.0, 2.0*Math.PI)
        Dot(0.3*sin(polarAngle) * cos(azimuthAngle), 0.3*sin(polarAngle) * 0.3*sin(azimuthAngle), cos(polarAngle))
    }

    fun updateDots() {
        allDots.forEach { dot1 ->
            //            dot1.vely = myrand.nextDouble(-1.0,1.0) - dot1.y
//            dot1.velx = myrand.nextDouble(-1.0,1.0) - dot1.x

            allDots.forEach { dot2 ->
                if (dot1.x != dot2.x || dot1.y != dot2.y) {
                    val dist = Math.sqrt(Math.pow(dot1.x - dot2.x, 2.0) + Math.pow(dot1.y - dot2.y, 2.0) + Math.pow(dot1.z - dot2.z, 2.0))
                    val dminus = dist*2.0 - 3.0
                    val pullconst = pullFactor * dminus*dminus / numDots * dot2.pullVal
                    dot1.velx += (dot2.x - dot1.x) * pullconst
                    dot1.vely += (dot2.y - dot1.y) * pullconst
                    dot1.velz += (dot2.z - dot1.z) * pullconst

                    val spinconstxy = spinFactor * dminus * dminus / numDots * dot2.spinValXY
                    dot1.velx += (dot2.y - dot1.y) * dot2.spinValXY * spinconstxy
                    dot1.vely -= (dot2.x - dot1.x) * dot2.spinValXY * spinconstxy
                    val spinconstyz = spinFactor * dminus * dminus / numDots * dot2.spinValYZ
                    dot1.vely += (dot2.z - dot1.z) * dot2.spinValYZ * spinconstyz
                    dot1.velz -= (dot2.y - dot1.y) * dot2.spinValYZ * spinconstyz
                    val spinconstzx = spinFactor * dminus * dminus / numDots * dot2.spinValZX
                    dot1.velz += (dot2.x - dot1.x) * dot2.spinValZX * spinconstzx
                    dot1.velx -= (dot2.z - dot1.z) * dot2.spinValZX * spinconstzx

                    val pushconst = -dminus*dminus*dminus / numDots * pushFactor * dot2.pushVal
                    dot1.velx -= sign(dot2.x - dot1.x) * pushconst
                    dot1.vely -= sign(dot2.y - dot1.y) * pushconst
                    dot1.velz -= sign(dot2.z - dot1.z) * pushconst
                }
            }
        }

        val mx = allDots.sumByDouble { it.x } / numDots.toDouble()
        val my = allDots.sumByDouble { it.y } / numDots.toDouble()
        val mz = allDots.sumByDouble { it.z } / numDots.toDouble()
        var totvel = 0.0

        allDots.forEach {
            it.velx -= (it.x - it.x0) * centerPullFactor
            it.vely -= (it.y - it.y0) * centerPullFactor
            it.velz -= (it.z - it.z0) * centerPullFactor
            it.velx *= frictionFactor
            it.vely *= frictionFactor
            it.velz *= frictionFactor

            totvel += abs(it.velx) + abs(it.vely) + abs(it.velz)
        }

        val avgvel = totvel / numDots
        val skewness = allDots.map { abs(it.velx) + abs(it.vely) + abs(it.velz) }.count { it < avgvel*0.9 }.toDouble() / numDots.toDouble()
        velCorrectionFactor += Math.max(Math.min(velCorrectionFactorSpeed * velCorrectionFactor, velCorrectionFactorSpeed * velCorrectionFactor * (targetAvgVel - avgvel)), -velCorrectionFactorSpeed * velCorrectionFactor)
//        println("$avgvel, $velCorrectionFactor, $skewness")

        allDots.forEach {
            it.velx *= velCorrectionFactor * (skewness * 2.0)
            it.vely *= velCorrectionFactor * (skewness * 2.0)
            it.velz *= velCorrectionFactor * (skewness * 2.0)

            it.x += it.velx * deltaT * velFactor - mx
            if (it.x < -dotPositionCutoff) {
                it.x = -dotPositionCutoff
                it.velx = -it.velx * wallBounceFactor
            } else if (it.x > dotPositionCutoff) {
                it.x = dotPositionCutoff
                it.velx = -it.velx * wallBounceFactor
            }

            it.y += it.vely * deltaT * velFactor - my
            if (it.y < -dotPositionCutoff) {
                it.y = -dotPositionCutoff
                it.vely = -it.vely * wallBounceFactor
            } else if (it.y > dotPositionCutoff) {
                it.y = dotPositionCutoff
                it.vely = -it.vely * wallBounceFactor
            }

            it.z += it.velz * deltaT * velFactor - mz
            if (it.z < -dotPositionCutoff) {
                it.z = -dotPositionCutoff
                it.velz = -it.velz * wallBounceFactor
            } else if (it.z > dotPositionCutoff) {
                it.z = dotPositionCutoff
                it.velz = -it.velz * wallBounceFactor
            }
//            val d02 = ((it.x - it.x0) * (it.x - it.x0) + (it.y - it.y0) * (it.y - it.y0)) * radFactor
//            val d02c = Math.min(1.0, Math.max(0.0, d02))
//            it.z = it.z0 + d02c * 2.0 - 1.0
        }

//        val mz = allDots.sumByDouble { it.z } / numDots.toDouble()

//        allDots.forEach {
//            it.z -= mz
//        }

        allDots.forEach {
//            it.pushVal = if (myrand.nextDouble() < pushJumpProb) it.pushVal * pushValFadeRate else pushValJump
//            it.pullVal = if (myrand.nextDouble() < pullJumpProb) it.pullVal * pullValFadeRate else pullValJump
//            it.pullVal = if (myrand.nextDouble() < superPullJumpProb) it.pullVal else superPullJumpVal
            it.spinValXY += it.spinValVel
            it.spinValYZ += it.spinValVel
            it.spinValZX += it.spinValVel
            it.spinValVel += -it.spinValXY * spinValChangeSpeed
            val newx0 = it.x0 + homeDragSpeed * (it.x - it.x0)
            val newy0 = it.y0 + homeDragSpeed * (it.y - it.y0)
            val newz0 = it.z0 + homeDragSpeed * (it.z - it.z0)
            val stayBig = (abs(it.x0) + abs(it.y0) + abs(it.z0)) / (abs(newx0) + abs(newy0) + abs(newz0))

            it.x0 = newx0 * stayBig
            it.y0 = newy0 * stayBig
            it.z0 = newz0 * stayBig
        }
    }

    fun updateDotColors() {

        allDots.forEach { d ->
            val cv = (abs(d.velx) + abs(d.vely)) * cvFactor
//            val cv = (d.spinVal + 0.5) * cvFactor
            val cvc = Math.min(1.0, Math.max(0.0, cv * 2.0))
//            val cv2 = Math.atan(d.pullVal / d.pushVal) / (Math.PI / 2.0)
            val d02 = ((d.x - d.x0) * (d.x - d.x0) + (d.y - d.y0) * (d.y - d.y0)) * radFactor
            val d02c = Math.min(1.0, Math.max(0.0, d02))
//            d.fillColor = Color.color(1.0-cvc, 1.0-cvc, 0.0 )
//                circle.radius = dotRadius + (1.0 - cvc) * 2.0
            d.fillColor = audioColorMap.colorMapValue(cvc)
            d.radius = maxDotRadius - d02c * (maxDotRadius - minDotRadius)
            d.colorMapVal = cv
            d.radiusVal = d02
        }

        cvFactor += cvFactor * 0.05 * (1.0 - allDots.map { it.colorMapVal }.max()!!)
//        println("[${allDots.map { it.colorMapVal }.min()}, ${allDots.map { it.colorMapVal }.max()}]")
        radFactor += radFactor * 0.05 * (1.0 - allDots.map { it.radiusVal }.max()!!)
//        println("[${allDots.map { it.radiusVal }.min()}, ${allDots.map { it.radiusVal }.max()}]")
    }

    var dotMoveTimer = timer(daemon = true, initialDelay = 1500, period = (1000.0/ physicsFrameRate).toLong()) {
        if (isRunning) {
            updateDots()
            updateDotColors()
        }
    }

    var changeDeltaTTimer = timer(daemon = true, initialDelay = 1500, period = (1000.0 / physicsFrameRate).toLong()) {
        if (isRunning)
            deltaT += deltaDeltaT
    }

    var moveHomeTimer = timer(daemon = true, initialDelay = 1500, period = (1000.0 / moveHomeRate).toLong()) {
        allDots.forEach {
//            it.x0 = it.x
//            it.y0 = it.y
            it.z0 = it.z / 2.0
        }
        audioColorMap.useNextColorMap()
    }

    fun playPause() {
        isRunning = !isRunning
        audioColorMap.isRunning = isRunning
    }

}
