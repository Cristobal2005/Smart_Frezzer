package com.example.smartfrezzer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SeleccionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion)

        val btnSensor = findViewById<Button>(R.id.btnSensor)
        val btnMonitor = findViewById<Button>(R.id.btnMonitor)


        btnSensor.setOnClickListener {
            startActivity(Intent(this, SensorActivity::class.java))
        }


        btnMonitor.setOnClickListener {
            startActivity(Intent(this, MonitorActivity::class.java))
        }
    }
}