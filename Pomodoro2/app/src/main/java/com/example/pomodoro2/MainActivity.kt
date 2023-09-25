package com.example.pomodoro2

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.pomodoro2.models.MealInfo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import android.graphics.Rect
import android.util.Size


class MainActivity : ComponentActivity() {

    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest
    lateinit var imageReader: ImageReader
    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        get_permissions()

        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                val aspectRatio: Float = 1080f / 1200f
                val newHeight: Int = (p1 / aspectRatio).toInt()

                val layoutParams = textureView.layoutParams
                layoutParams.width = p1
                layoutParams.height = newHeight
                textureView.layoutParams = layoutParams

                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    open_camera()
                }
            }
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }
        }

        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener(object: ImageReader.OnImageAvailableListener{
            override fun onImageAvailable(p0: ImageReader?) {
                var image = p0?.acquireLatestImage()
                var buffer = image!!.planes[0].buffer
                var bytes = ByteArray(buffer.remaining())

                buffer.get(bytes)
                sendImageToServer(bytes)

                image.close()
            }
        }, handler)

        findViewById<ImageButton>(R.id.capture).apply {
            setOnClickListener {
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                capReq.addTarget(imageReader.surface)
                cameraCaptureSession.capture(capReq.build(), null, null)
            }
        }
    }

    private fun close_camera() {
        if (::cameraCaptureSession.isInitialized) {
            cameraCaptureSession.close()
        }

        if (::cameraDevice.isInitialized) {
            cameraDevice.close()
        }

        if (::imageReader.isInitialized) {
            imageReader.close()
        }

        // Stellen Sie sicher, dass Sie auch den HandlerThread ordnungsgemäß beenden.
        if (::handlerThread.isInitialized) {
            handlerThread.quitSafely()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            open_camera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            close_camera()
        }
    }

    @SuppressLint("MissingPermission")
    fun open_camera(){
        val surfaceTexture = textureView.surfaceTexture ?: return
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                var capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                val desiredPreviewSize = Size(1080, 1920)

                //val surfaceTexture = textureView.surfaceTexture
                surfaceTexture?.setDefaultBufferSize(desiredPreviewSize.width, desiredPreviewSize.height)
                var surface = Surface(surfaceTexture)
                capReq.addTarget(surface)


                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                    }
                    override fun onConfigureFailed(p0: CameraCaptureSession) {

                    }
                }, handler)
            }
            override fun onDisconnected(p0: CameraDevice) {

            }
            override fun onError(p0: CameraDevice, p1: Int) {

            }
                                                                                                    }, handler)

    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog?.setMessage("Laden...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }


    fun get_permissions(){
        var permissionsLst = mutableListOf<String>()

        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsLst.add(android.Manifest.permission.CAMERA)

        if(permissionsLst.size > 0){
            requestPermissions(permissionsLst.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.e("NetworkError", "Camera open")
                close_camera()
                open_camera()
            } else {

            }
        }
    }
    private fun sendImageToServer(imageBytes: ByteArray) {
        showProgressDialog()
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "img.jpeg", imageBytes.toRequestBody("image/jpeg".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("https://eaf8-62-216-204-163.ngrok.io/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkError", "Anfrage fehlgeschlagen", e)
                runOnUiThread {
                    hideProgressDialog()
                    Toast.makeText(this@MainActivity, "NetworkError: Anfrage fehlgeschlagen", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                if (response.code != 200) {
                    Log.e("NetworkError", "Serverantwort mit Fehlercode: ${response.code}")
                    Log.e("NetworkSuccess", "Antwort vom Server: $responseBodyString")
                    if (responseBodyString != null) {
                        try {
                            val jsonObject = JSONObject(responseBodyString)
                            val errorMsg = jsonObject.optString("msg")
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                                hideProgressDialog()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            runOnUiThread {
                                hideProgressDialog()
                            }
                        }
                    }
                } else {
                    val gson = Gson()
                    val mealInfo: MealInfo = gson.fromJson(responseBodyString, MealInfo::class.java)
                    val intent = Intent(this@MainActivity, SecondActivity::class.java)

                    Log.e("NetworkSuccess", "Antwort vom Server: $responseBodyString")

                    intent.putExtra("response", mealInfo)
                    startActivity(intent)
                    runOnUiThread {
                        hideProgressDialog()
                    }
                }

            }
        })
    }
}

