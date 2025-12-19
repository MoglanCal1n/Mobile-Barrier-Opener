package com.example.barrieropener

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnManual = findViewById<Button>(R.id.btnDeschideBariera)

        btnManual.setOnClickListener {
            Toast.makeText(this, "Modul manual necesită implementare separată sau contact direct", Toast.LENGTH_SHORT).show()
        }

        verificaSiCerePermisiuni()
    }

    private fun pornesteServiciuMonitorizare() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "Monitorizare pornită (HUAWEI Safe)", Toast.LENGTH_SHORT).show()
    }

    private fun verificaSiCerePermisiuni() {
        val permisiuni = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permisiuni.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        ActivityCompat.requestPermissions(this, permisiuni.toTypedArray(), 101)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        } else {
            pornesteServiciuMonitorizare()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pornesteServiciuMonitorizare()
        }
    }
}