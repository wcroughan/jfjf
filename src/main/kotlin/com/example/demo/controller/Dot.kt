package com.example.demo.controller

import com.example.demo.view.minDotRadius
import javafx.scene.paint.Color

class Dot(var x0 : Double = myrand.nextDouble(-1.0, 1.0) * dotInitialSpread,
          var y0 : Double = myrand.nextDouble(-1.0, 1.0) * dotInitialSpread,
          var z0 : Double = 0.0) {
    //        var x = myrand.nextDouble(-1.0,1.0)
//        var y = myrand.nextDouble(-1.0,1.0)
    var x = x0
    var y = y0
    var z = z0
    var velx = myrand.nextDouble()
    var vely = myrand.nextDouble()
    var velz = 0.0
    var pushVal = myrand.nextDouble(0.5, 2.0)
    var pullVal = myrand.nextDouble(0.5, 2.0)
    var spinVal = myrand.nextDouble(-0.0, 1.0)
    var spinValVel = 0.0
    var fillColor = Color.BLACK
    var colorMapVal = 0.0
    var radiusVal = 0.0
    var radius = minDotRadius
}

