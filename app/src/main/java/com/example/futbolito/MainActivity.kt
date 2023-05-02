package com.example.futbolito

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

private var ancho: Int? = null
private var altura: Int? = null
private var e1: Int = 0
private var e2: Int = 0

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var sensorAcelerometer: Sensor? = null
    private var mSensor: Sensor? = null
    private lateinit var sensorManager: SensorManager
    lateinit var miViewDibujado: MiViewDibujado
/*  es llamada cuando la actividad es creada,
y se encarga de establecer la vista en
pantalla completa, obtener las dimensiones de la
pantalla, crear la vista personalizada MiViewDibujado,
y registrar los sensores en el SensorManager.
 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        // Ocultar la barra de título
        supportActionBar?.hide()
        // Establecer la vista de la actividad en pantalla completa
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Establecer el contenido de la actividad
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        altura = displayMetrics.heightPixels
        ancho = displayMetrics.widthPixels

        miViewDibujado = MiViewDibujado(this)
        setContentView(miViewDibujado)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            val gravSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_GRAVITY)
            mSensor =
                gravSensors.firstOrNull { it.vendor.contains("Google LLC") && it.version == 3 }
        }
        if (mSensor == null) {
            mSensor = if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                null
            }
        }

        sensorAcelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }
/*es llamada cuando la actividad se reanuda después de
haber estado en pausa. Registra el sensor de acelerómetro
para recibir actualizaciones en MiViewDibujado*/
    override fun onResume() {
        super.onResume()
        sensorAcelerometer?.also {
            sensorManager.registerListener(
                miViewDibujado, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }
/*cancela el procesos de sensor y hace una pausa*/
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(miViewDibujado)
    }
/*Cancela la registración del sensor de acelerómetro.*/
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(miViewDibujado)
    }
}
/*es una vista personalizada que extiende la clase View
 y implementa la interfaz SensorEventListener.
 Se encarga de dibujar la pelota y el marcador en
 la pantalla, y actualizar la posición de la pelota
  según las lecturas del sensor de acelerómetro.
 */
class MiViewDibujado(ctx: Context) : View(ctx), SensorEventListener {
    var xPos = ancho!! / 2f
    var yPos = altura!! / 2f
    var xAcceleration: Float = 0f
    var xVelocity: Float = 0.0f
    var yAcceleration: Float = 0f
    var yVelocity: Float = 0.0f
    var radio = 50f

    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.fubol)
    val ballmap = BitmapFactory.decodeResource(resources, R.drawable.ball)

    val canvasRect = Rect(0, 0, ancho!!, altura!!)
    val bitmapRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

    var pincel = Paint()
    var pincel2 = Paint()
    private var gravity = FloatArray(3)
    private var linear_acceleration = FloatArray(3)

    init {
        pincel.color = Color.WHITE
        pincel2.color = Color.BLACK
        pincel2.textSize = 80f
        pincel2.color = Color.WHITE
    }
/*dibuja la img de fondo, marcador y la pelota*/
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitmapRect.offsetTo(
            canvasRect.centerX() - bitmapRect.width() / 2,
            canvasRect.centerY() - bitmapRect.height() / 2
        )

        canvas!!.drawBitmap(bitmap, null, canvasRect, null)
        canvas.drawBitmap(ballmap, null, RectF(xPos - radio, yPos - radio, xPos + radio, yPos + radio), null)
        canvas.drawText("$e1:$e2", ancho!! / 12f, altura!! / 17f, pincel2)

        invalidate()
    }
    /*es llamada cuando hay una nueva lectura
    del sensor de acelerómetro. Calcula la aceleración
     lineal en los ejes x e y, y actualiza la velocidad
      y posición de la pelota en consecuencia.*/

    override fun onSensorChanged(event: SensorEvent?) {
        //TODO("Not yet implemented")
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        val alpha = 0.8f

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event!!.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        linear_acceleration[0] = event.values[0] - gravity[0]   //x
        linear_acceleration[1] = event.values[1] - gravity[1]    //y
        linear_acceleration[2] = event.values[2] - gravity[2]   //z

        moverPelota(linear_acceleration[0], linear_acceleration[1] * -1)
    }

    private fun moverPelota(xOrientation: Float, yOrientation: Float) {
        //TODO("Not yet implemented")
        xAcceleration = xOrientation
        yAcceleration = yOrientation
        updateX()
        updateY()
        gol()
    }

    fun updateX() {
        if (xPos < ancho!! - radio && xPos > 0 + radio) {
            xVelocity -= xAcceleration * 3f
            xPos += xVelocity
        } else if (xPos >= ancho!! - radio) {
            xPos = ancho!! - radio * 2 + 1
            xVelocity -= xAcceleration * 3f
            xPos += xVelocity
        } else if (xPos <= 0 + radio) {
            xPos = radio * 2 + 1
            xVelocity -= xAcceleration * 3f
            xPos += xVelocity
        }
    }

    fun updateY() {
        if (yPos < altura!! - radio && yPos > 0 + radio) {
            yVelocity -= yAcceleration * 3f
            yPos += yVelocity
        } else if (yPos >= altura!! - radio) {
            yPos = altura!! - radio * 3 + 50f
            yVelocity -= yAcceleration * 3f
            yPos += yVelocity
        } else if (yPos <= 0 + radio) {
            yPos = radio * 3 + 50f
            yVelocity -= yAcceleration * 3f
            yPos += yVelocity
        }
    }

    fun gol() {
        if (yPos >= altura!! - radio * 2 && (xPos <= ancho!! / 2f + 50 && xPos >= ancho!! / 2f - 50)) {
            e1++
            xPos = ancho!! / 2f
            yPos = altura!! / 2f
        }
        if (yPos <= 0 + radio * 2 && (xPos <= ancho!! / 2f + 50 && xPos >= ancho!! / 2f - 50)) {
            e2++
            xPos = ancho!! / 2f
            yPos = altura!! / 2f
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }
}