package com.example.flamapp

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder

class SimpleCamera(private val ctx: Context, private val onFrame: (ByteArray, Int, Int) -> Unit) {
    private var camera: Camera? = null
    private var previewSize: Camera.Size? = null

    fun startPreview(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            val params = camera!!.parameters
            // choose a preview size (prefer 640x480)
            var chosen: Camera.Size? = null
            for (s in params.supportedPreviewSizes) {
                if (s.width == 640 && s.height == 480) { chosen = s; break }
            }
            if (chosen == null) chosen = params.supportedPreviewSizes[0]
            previewSize = chosen
            params.setPreviewSize(chosen.width, chosen.height)
            params.previewFormat = android.graphics.ImageFormat.NV21
            camera!!.parameters = params
            camera!!.setPreviewDisplay(holder)
            camera!!.setPreviewCallback { data, cam ->
                // data: NV21 byte array
                val w = previewSize!!.width
                val h = previewSize!!.height
                onFrame(data, w, h)
            }
            camera!!.startPreview()
        } catch (e: Exception) {
            Log.e("SimpleCamera", "startPreview error: ${'$'}e")
        }
    }

    fun stop() {
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
    }
}
