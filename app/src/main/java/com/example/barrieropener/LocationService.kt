package com.example.barrieropener

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

class LocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val channelId = "BarrierServiceChannel"

    val targetLat= 47.629732
    val targetLng = 26.220627
    private val razaActiune = 150f
    private var apelEfectuat = false
    private val numeBariera = "Bariera"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        porneșteMonitorizarea()
    }

    private fun porneșteMonitorizarea() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 10f, this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 10f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        val targetLocation = Location("target")
        targetLocation.latitude = targetLat
        targetLocation.longitude = targetLng

        val distanta = location.distanceTo(targetLocation)

        if (distanta <= razaActiune) {
            if (!apelEfectuat) {
                apeleazaBariera()
                apelEfectuat = true
            }
        } else {
            if (distanta > (razaActiune * 2)) {
                apelEfectuat = false
            }
        }
    }

    private fun apeleazaBariera() {
        val numar = gasesteNumarDupaNume(numeBariera)
        if (numar != null) {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$numar")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun gasesteNumarDupaNume(numeCautat: String): String? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?"
        val args = arrayOf(numeCautat)
        val cursor = contentResolver.query(uri, null, selection, args, null)

        var numar: String? = null
        if (cursor?.moveToFirst() == true) {
            val index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (index >= 0) numar = cursor.getString(index)
        }
        cursor?.close()
        return numar
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Monitorizare Barieră Activă")
            .setContentText("Verific locația...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Serviciu Bariera", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this) // Oprim GPS-ul când închidem serviciul
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}