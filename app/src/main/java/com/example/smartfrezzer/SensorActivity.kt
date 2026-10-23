package com.example.smartfrezzer

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class SensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensorLuz: Sensor? = null
    private lateinit var tvEstadoPuerta: TextView
    private lateinit var tvLux: TextView


    private val database = FirebaseDatabase.getInstance().getReference("refri/puerta_abierta")
    private var estaAbierto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        tvEstadoPuerta = findViewById(R.id.tvEstadoPuerta)
        tvLux = findViewById(R.id.tvLux)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        super.onResume()
        sensorLuz?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            tvLux.text = "Luz: ${lux.toInt()} lx"


            if (lux > 10f && !estaAbierto) {
                estaAbierto = true
                tvEstadoPuerta.text = "ABIERTO"
                tvEstadoPuerta.setTextColor(Color.parseColor("#D32F2F")) // Rojo
                database.setValue(true)
            }

            else if (lux <= 5f && estaAbierto) {
                estaAbierto = false
                tvEstadoPuerta.text = "CERRADO"
                tvEstadoPuerta.setTextColor(Color.parseColor("#2E7D32")) // Verde
                database.setValue(false)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}