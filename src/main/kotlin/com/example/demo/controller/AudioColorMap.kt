package com.example.demo.controller

import javafx.scene.paint.Color
import javax.sound.sampled.*
import kotlin.concurrent.thread

val cmap1rx = arrayOf(0.0, 0.5)
val cmap1rv = arrayOf(0.0, 1.0)
val cmap1gx = arrayOf(0.5, 0.75)
val cmap1gv = arrayOf(0.0, 1.0)
val cmap1bx = arrayOf(0.75, 1.0)
val cmap1bv = arrayOf(0.0, 1.0)

val cmap2rx = arrayOf(0.0, 0.25)
val cmap2rv = arrayOf(1.0, 0.0)
val cmap2gx = arrayOf(0.25, 0.5)
val cmap2gv = arrayOf(1.0, 0.0)
val cmap2bx = arrayOf(0.5, 1.0)
val cmap2bv = arrayOf(1.0, 0.0)

class AudioColorMap {
    var isRunning = true
    var colormapIdx = 0

    val audioFormat = AudioFormat(44100.0f, 16, 1, true, true)
    val dli = DataLine.Info(TargetDataLine::class.java, audioFormat)
    val AUDIO_BUFFER_SIZE = 441000
    val AUDIO_BUFFER_READ_SIZE = 44100
    val audioInArray = ByteArray(AUDIO_BUFFER_SIZE)

    init {
        if (AudioSystem.isLineSupported(dli)) {
            try {
                val targetDataLine = AudioSystem.getTargetDataLine(audioFormat)
                targetDataLine.open()
                targetDataLine.start()

                thread(isDaemon = true) {
                    while (true) {
                        if (isRunning) {
                            val numBytesRead = targetDataLine.read(audioInArray, 0, AUDIO_BUFFER_READ_SIZE)
//                            println("$numBytesRead, first byte is ${audioInArray[0]}")
//                            isFlipped = audioInArray[0].toInt() == 0
                            if (audioInArray[0].toInt() == 0)
                                colormapIdx = (colormapIdx + 1) % 3
                        }
                    }
                }
            } catch (e : LineUnavailableException) {
                println("Couldn't open line:\n\t$e")
            }
        }
    }

    //color value lies in [0.0, 1.0]
    fun colorMapValue(cvc: Double): Color {
        val r = interpValue(cvc, cmap1rx, cmap1rv)
        val g = interpValue(cvc, cmap1gx, cmap1gv)
        val b = interpValue(cvc, cmap1bx, cmap1bv)
//        if (isFlipped)
//            return Color.color(interpValue(cvc, cmap1rx, cmap1rv),
//                    interpValue(cvc, cmap1gx, cmap1gv),
//                    interpValue(cvc, cmap1bx, cmap1bv))
//        else
//            return Color.color(interpValue(cvc, cmap2rx, cmap2rv),
//                    interpValue(cvc, cmap2gx, cmap2gv),
//                    interpValue(cvc, cmap2bx, cmap2bv))
        var c1 = r
        var c2 = g
        var c3 = b
        if (colormapIdx == 1) {
            c1 = g
            c2 = b
            c3 = r
        } else if (colormapIdx == 2) {
            c1 = b
            c2 = r
            c3 = g
        }
        return Color.color(c1, c2, c3)


//        return Color.color(1.0-cvc, cvc, 0.0)
//        return Color.color(cvc, cvc*0.8, 0.0)

//        var r = Math.min(1.0, cvc * 3.0)
//        var g = Math.min(1.0, Math.max(0.0, cvc * 3.0 - 1.0))
//        var b = Math.min(1.0, Math.max(0.0, cvc * 3.0 - 2.0))
//        var r = interpValue(cvc, cmap1rx, cmap1rv)
//        var g = interpValue(cvc, cmap1gx, cmap1gv)
//        var b = interpValue(cvc, cmap1bx, cmap1bv)

//        if (isFlipped) {
//            r = 1.0 - r
//            g = 1.0 - g
//            b = 1.0 - b
//        }
//        if (b > 0.5) {
//            g = 2.0 - 2.0 * b
//            r = 2.0 - 2.0 * b
//            b = 1.0 - b
//        }
//        return Color.color(r,g,b)
    }

    fun interpValue(x : Double, xref : Array<Double>, vref : Array<Double>): Double {
        val firstOverIdx = (0 until xref.size).find { xref[it] > x } ?: xref.size-1
        val lastUnderIdx = (0 until xref.size).findLast { xref[it] < x } ?: 0
        if (firstOverIdx == lastUnderIdx)
            return vref[lastUnderIdx]
        val x1 = xref[lastUnderIdx]
        val x2 = xref[firstOverIdx]
        val v1 = vref[lastUnderIdx]
        val v2 = vref[firstOverIdx]
        return v1 + (v2 - v1) * (x - x1) / (x2 - x1)
    }

//    init {
//        val a = (0..20).map { i -> i.toDouble() / 20.0 }
//        val r = a.map { interpValue(it, cmap1rx, cmap1rv) }
//        val g = a.map { interpValue(it, cmap1gx, cmap1gv) }
//        val b = a.map { interpValue(it, cmap1bx, cmap1bv) }
//
//        println("${a.joinToString()}")
//        println("${r.joinToString()}")
//        println("${g.joinToString()}")
//        println("${b.joinToString()}")
//    }

}