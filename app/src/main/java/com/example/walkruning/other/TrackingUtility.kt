package com.example.walkruning.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.walkruning.service.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit


object TrackingUtility {

    // TrackingFragment içerisindeki Harita için  izinlerini Hallettik.
    fun hasLocationPermissions(context: Context) =
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                EasyPermissions.hasPermissions(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            } else {
                EasyPermissions.hasPermissions(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
            }

    // TODO: 23.06.2021 haritada kullanıcıya göre hareket eden çizgileri konumlarından hesaplatıp bir uzunluğa sahip olduk.
    fun calculatePolylineLength(polyline: Polyline): Float {
        var distance = 0f
        for (i in 0..polyline.size -2){
            val position1 = polyline[i]
            val position2 = polyline[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(
                    position1.latitude,
                    position1.longitude,
                    position2.latitude,
                    position2.longitude,
                    result

            )
            distance += result[0]
        }
        return distance
    }



    // TODO: 21.06.2021 Koronometre İşlemleri
    // Formatlanmış(Biçimlendirilmiş) Kronometre Süresini Almak için. Yani Kronometre Çalışması için Saat dakika saniye milisaniyelerin kurulumunu yaptık.
    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false):String {
        var milliseconds = ms

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if (!includeMillis){
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"

    }
}