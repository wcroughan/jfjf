package com.example.demo.controller

import javafx.scene.paint.Color
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.io.File
import java.nio.ByteBuffer
import javax.sound.sampled.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer

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

val minfreq = 200
val maxfreq = 1200

val colorShiftSpeed = 0.002


class AudioColorMap {
    var isRunning = true
    var colormapIdx = 0
    var colorScaleFactor = 0.0
    var colorShiftAmount = 0.0

    val sampleRate = 44100.0
    val audioFormat = AudioFormat(sampleRate.toFloat(), 16, 1, true, true)
    val dli = DataLine.Info(TargetDataLine::class.java, audioFormat)
    val AUDIO_BUFFER_SIZE = 1024 * 8
    val audioInArray = ByteArray(AUDIO_BUFFER_SIZE)
    val outFile = File("/home/wcroughan/Desktop/audio_out.dat")
    val audio = ShortArray(AUDIO_BUFFER_SIZE / 2)
    val fft = FastFourierTransformer(DftNormalization.STANDARD)

    init {
        outFile.writeBytes(ByteArray(0))
        println("launching audio interpretter")
        if (AudioSystem.isLineSupported(dli)) {
            try {
                val targetDataLine = AudioSystem.getTargetDataLine(audioFormat)
                println("Got data line")
                targetDataLine.open()
                println("Opened data line")
//                println("${targetDataLine.controls.size}")
//                targetDataLine.getControl()
                targetDataLine.start()
                println("Started data line")

                thread(isDaemon = true) {
                    while (true) {
                        if (isRunning) {
                            val numBytesRead = targetDataLine.read(audioInArray, 0, AUDIO_BUFFER_SIZE)
//                            println("$numBytesRead, first byte is ${audioInArray[0]}")
//                            isFlipped = audioInArray[0].toInt() == 0
//                            if (audioInArray[0].toInt() == 0)
//                                colormapIdx = (colormapIdx + 1) % 3

//                            outFile.appendBytes(audioInArray.sliceArray(IntRange(0, AUDIO_BUFFER_SIZE-1)))
//                            println("Wrote $numBytesRead bytes")

                            if (numBytesRead > 0) {
//                                println("read $numBytesRead bytes")
                                ByteBuffer.wrap(audioInArray).asShortBuffer().get(audio)
                                val topToBottom = audio.max()!! - audio.min()!!
//                                println("$topToBottom")

                                val fftout = fft.transform(audio.map { it.toDouble() }.toDoubleArray(), TransformType.FORWARD)
//                                outFile.appendText(fftout.joinToString() { "${it.real}, ${it.imaginary}, " } )
//                                outFile.appendText("\n")

                                val periodogram = fftout.map { Math.pow(it.abs(), 2.0) }
//                                outFile.appendText(periodogram.joinToString() + "\n")
                                val pfi = periodogram.indices.maxBy { periodogram[it] }!!
                                val peakfreq = pfi * sampleRate / AUDIO_BUFFER_SIZE.toDouble()
//                                println("peak freq = $peakfreq, magnitude = ${periodogram[pfi]}")
                                colorScaleFactor = Math.min(1.0, Math.max(peakfreq - minfreq, 0.0) / (maxfreq - minfreq))
//                                println("$colorScaleFactor")

                            } else {
//                                println("No audio input")
                            }
                        } else {
//                            println("Not running")
                        }
                    }
                }
            } catch (e : LineUnavailableException) {
                println("Couldn't open line:\n\t$e")
            }
        } else {
            println("Audio device not supported")
        }
    }

    //color value lies in [0.0, 1.0]
    fun colorMapValue(cvc: Double): Color {
//        val cvscaled = cvc * colorScaleFactor
        val cvscaled = cvc
        val r = interpValue(cvscaled, cmap1rx, cmap1rv)
        val g = interpValue(cvscaled, cmap1gx, cmap1gv)
        val b = interpValue(cvscaled, cmap1bx, cmap1bv)
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
        val col = Color.color(c1, c2, c3)
        return col.deriveColor(360 * colorShiftAmount, 1.0, 1.0, 1.0)


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

    fun useNextColorMap() {
//        colormapIdx = (colormapIdx + 1) % 3
    }

    val colorShiftTimer = timer(daemon = true, period = (1000 / 30).toLong()) {
        if (isRunning)
            colorShiftAmount = (colorShiftAmount + colorShiftSpeed).rem(1.0)
    }

}