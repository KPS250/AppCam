package io.kps.appcam

object Utils {

    @JvmStatic
    public fun formatLoc(location : Double) : String{
        val num1Loc = Math.floor(location).toInt()
        val num2Loc = Math.floor((location - num1Loc) * 60).toInt()
        val num3Loc = (location - (num1Loc.toDouble() + num2Loc.toDouble() / 60)) * 3600000
        return  "$num1Loc/1,$num2Loc/1,$num3Loc/1000"
    }

    @JvmStatic
    public fun reformatLocation(location : String): Double{
        val arr = location.split(",")
        val num1 = arr[0].split("/1")[0].toDouble()
        val num2 = arr[1].split("/1")[0].toDouble()
        val num3 = arr[2].split("/1000")[0].toDouble()
        val num4 = num1+num2/60+num3/3600000
        return num4.roundTo(7)
    }

    @JvmStatic
    public fun Double.roundTo(n : Int) : Double {
        return "%.${n}f".format(this).toDouble()
    }
}