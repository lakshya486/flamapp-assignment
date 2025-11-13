package com.example.flamapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var surfaceView: SurfaceView
    private lateinit var imageView: ImageView
    private var cameraHelper: SimpleCamera? = null

    companion object {
        init { System.loadLibrary("native-lib") }
        const val TAG = "MainActivity"
        const val PERM_REQ = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // simple UI: SurfaceView (camera preview) + ImageView (processed result)
        surfaceView = SurfaceView(this)
        imageView = ImageView(this)
        setContentView(surfaceView)
        addContentView(imageView, android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT))

        if (!hasCameraPermission()) requestPermissions()
        else initCamera()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERM_REQ)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_REQ && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera()
        } else {
            Log.e(TAG, "Camera permission required")
        }
    }

    private fun initCamera() {
        cameraHelper = SimpleCamera(this) { data, w, h ->
            // call native to process frame (NV21 -> RGBA bytes)
            try {
                val out = NativeBridge.processFrame(data, w, h)
                // convert RGBA bytearray to Bitmap and display
                val bmp = rgbaToBitmap(out, w, h)
                runOnUiThread {
                    imageView.setImageBitmap(bmp)
                }
            } catch (e: Exception) {
                Log.e(TAG, "native process error: ${'$'}e")
            }
        }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                cameraHelper?.startPreview(holder)
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraHelper?.stop()
            }
        })
    }

    private fun rgbaToBitmap(bytes: ByteArray, w: Int, h: Int): Bitmap {
        // bytes: RGBA (r,g,b,a) per pixel
        val pixels = IntArray(w * h)
        var idx = 0
        for (i in 0 until w*h) {
            val r = bytes[idx++].toInt() and 0xFF
            val g = bytes[idx++].toInt() and 0xFF
            val b = bytes[idx++].toInt() and 0xFF
            val a = bytes[idx++].toInt() and 0xFF
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }
}
