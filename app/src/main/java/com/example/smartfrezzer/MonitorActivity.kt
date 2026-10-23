package com.example.smartfrezzer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class MonitorActivity : AppCompatActivity() {

    private lateinit var tvEstadoRed: TextView
    private lateinit var tvTimer: TextView
    private lateinit var etNotif: EditText
    private lateinit var etVibra: EditText
    private lateinit var etAlarma: EditText


    private val database = FirebaseDatabase.getInstance().getReference("refri/puerta_abierta")

    private var segundosAbierto = 0
    private var handler = Handler(Looper.getMainLooper())
    private var vibrator: Vibrator? = null
    private var ringtone: Ringtone? = null


    private val timerRunnable = object : Runnable {
        override fun run() {
            segundosAbierto++
            tvTimer.text = "${segundosAbierto}s"
            verificarAlertas()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)

        tvEstadoRed = findViewById(R.id.tvEstadoRed)
        tvTimer = findViewById(R.id.tvTimer)
        etNotif = findViewById(R.id.etNotif)
        etVibra = findViewById(R.id.etVibra)
        etAlarma = findViewById(R.id.etAlarma)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(applicationContext, alertUri)

        crearCanalNotificacion()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }


        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val abierto = snapshot.getValue(Boolean::class.java) ?: false
                if (abierto) {
                    iniciarAlerta()
                } else {
                    detenerAlerta()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun iniciarAlerta() {
        tvEstadoRed.text = "Estado: ABIERTO"
        tvEstadoRed.setTextColor(Color.parseColor("#D32F2F")) // Rojo

        // Solo iniciamos el contador si estaba en cero
        if (segundosAbierto == 0) {
            handler.post(timerRunnable)
        }
    }

    private fun detenerAlerta() {
        tvEstadoRed.text = "Estado: CERRADO"
        tvEstadoRed.setTextColor(Color.parseColor("#2E7D32")) // Verde


        handler.removeCallbacks(timerRunnable)
        segundosAbierto = 0
        tvTimer.text = "0s"

        vibrator?.cancel()
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
    }

    private fun verificarAlertas() {
        val tNotif = etNotif.text.toString().toIntOrNull() ?: 5
        val tVibra = etVibra.text.toString().toIntOrNull() ?: 10
        val tAlarma = etAlarma.text.toString().toIntOrNull() ?: 15


        if (segundosAbierto == tNotif) {
            lanzarNotificacion()
        }
        if (segundosAbierto == tVibra) {
            val patron = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(patron, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(patron, 0)
            }
        }

        if (segundosAbierto == tAlarma && ringtone?.isPlaying == false) {
            ringtone?.play()
        }
    }

    private fun lanzarNotificacion() {
        val builder = NotificationCompat.Builder(this, "alerta_refri")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("¡El refri está abierto!")
            .setContentText("Lleva mucho tiempo abierto, ve a cerrarlo.")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, builder.build())
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel("alerta_refri", "Alertas del Refrigerador", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }
}