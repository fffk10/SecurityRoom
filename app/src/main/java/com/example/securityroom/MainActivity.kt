package com.example.securityroom

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val threshold: Float = 10f
    private var oldValue: Float = 0f
    private lateinit var cameraManager: CameraManager

    /**
     * var で宣言した場合、Nullチェックを行った場合でもエラーが表示される
     * 回避するには val で宣言するか、let,エルビス演算子を使う
     */
    private var cameraID: String? = null
    private var lightOn: Boolean = false

    /**
     * センサー感度の変更を検知する
     * 変更はないので未実装
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    /**
     * 端末が倒れた場合にライトをつける
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val zDiff = abs(event.values[2] - oldValue)
            if (zDiff > threshold) {
                torchOn()
            }
            oldValue = event.values[2]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
            // トーチモードが変更された時の処理
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                cameraID = cameraId
                lightOn = enabled
            }
        }, Handler())
    }

    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)

        if (cameraID != null) {
            val notNullCameraID: String? = cameraID
            try {
                if (lightOn) {
                    notNullCameraID?.let { cameraManager.setTorchMode(it, false) }
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun torchOn() {
        if (cameraID != null) {
            val notNullCameraID: String? = cameraID
            try {
                if (!lightOn) {
                    notNullCameraID?.let { cameraManager.setTorchMode(it, true) }
                } else {
                    notNullCameraID?.let { cameraManager.setTorchMode(it, false) }
                }

            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}