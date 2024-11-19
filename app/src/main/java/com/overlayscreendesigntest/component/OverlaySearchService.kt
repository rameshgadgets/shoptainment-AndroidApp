package com.overlayscreendesigntest.component

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.overlayscreendesigntest.R
import com.overlayscreendesigntest.data.OverlayListResponse
import com.overlayscreendesigntest.data.SimilarProduct
import com.overlayscreendesigntest.networking.RetrofitClient
import com.overlayscreendesigntest.screens.WebViewActivity
import com.overlayscreendesigntest.screens.adapter.OverlayListAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class OverlaySearchService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlaySearchBtnView: View
    private lateinit var recyclerViewOverlay: View
    private lateinit var cancelView: View
    private val CHANNEL_ID = "ForegroundServiceChannel"
    private var isRecyclerViewVisible = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OverlayListAdapter
    private lateinit var progressBar: ProgressBar

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_MEDIA_PROJECTION_GRANTED") {
            val resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED)
            val data = intent.getParcelableExtra<Intent>("data")
            if (resultCode == RESULT_OK && data != null) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                setupMediaProjection()
            }
        }
        return START_STICKY
    }

    private fun setupMediaProjection() {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            0,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun captureScreenshot() {
        val image = imageReader?.acquireLatestImage() ?: return

        val planes = image.planes
        val buffer = planes[0].buffer
        val width = image.width
        val height = image.height
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        saveBitmapToStorage(bitmap)
    }

    private fun captureLegacyScreenshot() {
        try {
            val displayMetrics = DisplayMetrics()
            windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val rootView = View(this).rootView
            rootView.isDrawingCacheEnabled = true
            val bitmap = rootView.drawingCache
            saveBitmapToStorage(bitmap)
            rootView.isDrawingCacheEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error capturing screenshot", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Screenshot_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }
                showRecyclerViewOverlay(uri)
//                Toast.makeText(this, "Screenshot saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
//                Toast.makeText(this, "Failed to save screenshot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
//            Toast.makeText(this, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate() {
        super.onCreate()

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotificationChannel()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(1, createNotification())
        } else {
            startForeground(
                1, createNotification(),
                FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        }
//        startForeground(1, createNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate the floating widget layout
        overlaySearchBtnView = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.END or Gravity.BOTTOM
        params.x = 0
        params.y = 200


        // Set up the cancel view (Cancel button) but hide it initially
        cancelView = LayoutInflater.from(this).inflate(R.layout.cancel_btn, null)
        val cancelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        cancelParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        cancelParams.y = 100
        windowManager.addView(cancelView, cancelParams)
        cancelView.visibility = View.GONE



//        // Add the floating widget to the window
//        windowManager.addView(overlaySearchBtnView, params)

        setupRecyclerViewOverlay()

        // Add touch listener to move the widget around
        val btnSearch = overlaySearchBtnView.findViewById<AppCompatImageView>(R.id.btn_search)

        btnSearch.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            private var clickTime = 0L
            // Define a movement threshold to differentiate between click and drag (in pixels)
            private val CLICK_THRESHOLD = 10

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Save the initial position when touch down
//                        cancelView.visibility = View.VISIBLE
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        clickTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate the movement delta
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()

                        // Update the position with the delta
                        params.x = initialX - deltaX
                        params.y = initialY - deltaY

                        // Update the view layout with new coordinates
                        windowManager.updateViewLayout(overlaySearchBtnView, params)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        // Detect if it was a click based on the time difference and movement distance
                        val timeDifference = System.currentTimeMillis() - clickTime

                        // Check if the movement was minimal (i.e., a click, not a drag)
                        val movementX = (event.rawX - initialTouchX).toInt()
                        val movementY = (event.rawY - initialTouchY).toInt()
                        if (timeDifference < 200 && abs(movementX) < CLICK_THRESHOLD && abs(movementY) < CLICK_THRESHOLD) {
                            // It is a click if the movement and time are within the threshold
                            v?.performClick()
                        }

//                        if (isOverlapping(params, cancelParams)) {
//                            stopSelf()
//                        }
//                        cancelView.visibility = View.GONE
                        return true
                    }
                }
                return false
            }
        })

        // Add the floating widget to the window
        windowManager.addView(overlaySearchBtnView, params)

        btnSearch.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    startMediaProjection()
                captureScreenshot()
            } else {
                captureLegacyScreenshot()
            }

//            if (isRecyclerViewVisible) {
//                hideRecyclerViewOverlay()
//            } else {
//                showRecyclerViewOverlay()
//            }
        }
    }

    // Function to open a URL in the device's browser
    private fun openUrl(url: String) {
        try {
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", url)  // Pass the URL to the WebViewActivity
                flags = Intent.FLAG_ACTIVITY_NEW_TASK  // Add this flag when starting a new activity from a service
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(
                "Error Exception", e.message.toString()
            )
//            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchItemsFromApi(imageUri:Uri) {
        progressBar.visibility = View.VISIBLE
        val resolver = contentResolver
        try {
            // Convert URI to InputStream
            val inputStream = resolver.openInputStream(imageUri)
            val tempFile = File(cacheDir, "temp_image.png")

            // Copy the input stream to a temporary file
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

            val apiKeyBody = RequestBody.create("text/plain".toMediaTypeOrNull(), API_KEY)

            val call = RetrofitClient.api.fetchOverLayScreenItems(
                apiKey = apiKeyBody,
                image = filePart
            )
            call.enqueue(object : Callback<OverlayListResponse> {
                override fun onResponse(
                    call: Call<OverlayListResponse>,
                    response: Response<OverlayListResponse>
                ) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val items = response.body()
                        val allSimilarProduct = ArrayList<SimilarProduct>()
                        items?.data?.result_groups?.forEach { resultGroup ->
                            allSimilarProduct.addAll(resultGroup.similar_products)
                        }
//                        Toast.makeText(applicationContext, "Items", Toast.LENGTH_SHORT).show()
                        adapter.updateItems(allSimilarProduct)
//                        Toast.makeText(applicationContext, "Success to fetch items", Toast.LENGTH_SHORT).show()
                    } else {
//                        Toast.makeText(applicationContext, "Failed to fetch items", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OverlayListResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }

            })
        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(this, "Failed to prepare file: ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun setupRecyclerViewOverlay() {
        recyclerViewOverlay = LayoutInflater.from(this).inflate(R.layout.overlay_list_screen, null)

        progressBar = recyclerViewOverlay.findViewById(R.id.progressBar)
        val imgClose = recyclerViewOverlay.findViewById<AppCompatImageView>(R.id.img_close)
        imgClose.setOnClickListener {
            hideRecyclerViewOverlay()
        }
        // RecyclerView setup
        recyclerView = recyclerViewOverlay.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OverlayListAdapter(listOf()) { item ->
            openUrl(item.url)
            hideRecyclerViewOverlay()
        }
        recyclerView.adapter = adapter

        // Layout parameters for the RecyclerView overlay
        val recyclerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        recyclerParams.gravity = Gravity.CENTER
    }

    private fun showRecyclerViewOverlay(uri:Uri) {
//        if (!isRecyclerViewVisible) {
            // Dynamically calculate screen width and height
            val displayMetrics = DisplayMetrics()
            val display = windowManager.defaultDisplay
            display.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Setting desired width and height for the overlay screen
            val overlayWidth = (displayMetrics.widthPixels * 0.8).toInt()  // 80% of screen width
            val overlayHeight = (displayMetrics.heightPixels * 0.6).toInt()
            val recyclerParams = WindowManager.LayoutParams(
                (screenWidth * 0.7).toInt(),
                (screenHeight * 0.5).toInt(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            recyclerParams.gravity = Gravity.END

            windowManager.addView(recyclerViewOverlay, recyclerParams)
            isRecyclerViewVisible = true

            fetchItemsFromApi(uri)
//        }else{
//            fetchItemsFromApi(uri)
//        }
    }

    private fun hideRecyclerViewOverlay() {
        if (isRecyclerViewVisible) {
            windowManager.removeView(recyclerViewOverlay)
            isRecyclerViewVisible = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::overlaySearchBtnView.isInitialized) windowManager.removeView(overlaySearchBtnView)
        if (isRecyclerViewVisible) windowManager.removeView(recyclerViewOverlay)
        if (::cancelView.isInitialized) windowManager.removeView(cancelView)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Floating Widget Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating Widget Active")
            .setContentText("Your floating widget is running.")
            .setSmallIcon(R.drawable.notification)
            .build()
    }
}