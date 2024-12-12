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
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.overlayscreendesigntest.R
import com.overlayscreendesigntest.data.OverlayListResponse
import com.overlayscreendesigntest.data.SimilarProduct
import com.overlayscreendesigntest.networking.RetrofitClient
import com.overlayscreendesigntest.screens.HomeActivity
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
import java.io.IOException
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
    private lateinit var txtMessage: TextView
    private lateinit var webView: WebView

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

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

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startForegroundServiceWithNotification()
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            startForeground(1, createNotification())
//        } else {
//            startForeground(
//                1, createNotification(),
//                FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
//            )
//        }
        setUpSearchBtnOverlay()
//        startForeground(1, createNotification())

        // Inflate the floating widget layout
    }

    private fun setUpSearchBtnOverlay(){
        overlaySearchBtnView = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)

        val searchBtnParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        searchBtnParams.gravity = Gravity.END or Gravity.BOTTOM
        searchBtnParams.x = 0
        searchBtnParams.y = 200

//        // Set up the cancel view (Cancel button) but hide it initially
//        cancelView = LayoutInflater.from(this).inflate(R.layout.cancel_btn, null)
//        val cancelParams = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            PixelFormat.TRANSLUCENT
//        )
//        cancelParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
//        cancelParams.y = 100
//        windowManager.addView(cancelView, cancelParams)
//        cancelView.visibility = View.VISIBLE

//                setupRecyclerViewOverlay()

//        // Add the floating widget to the window
//        windowManager.addView(overlaySearchBtnView, cancelParams)

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
                        initialX = searchBtnParams.x
                        initialY = searchBtnParams.y
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
                        searchBtnParams.x = initialX - deltaX
                        searchBtnParams.y = initialY - deltaY

                        // Update the view layout with new coordinates
                        windowManager.updateViewLayout(overlaySearchBtnView, searchBtnParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        // Detect if it was a click based on the time difference and movement distance
                        val timeDifference = System.currentTimeMillis() - clickTime

                        // Check if the movement was minimal (i.e., a click, not a drag)
                        val movementX = (event.rawX - initialTouchX).toInt()
                        val movementY = (event.rawY - initialTouchY).toInt()
                        if (timeDifference < 200 && abs(movementX) < CLICK_THRESHOLD && abs(
                                movementY
                            ) < CLICK_THRESHOLD
                        ) {
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
        windowManager.addView(overlaySearchBtnView, searchBtnParams)
        btnSearch.setOnClickListener {
            windowManager.removeView(overlaySearchBtnView)
            setupRecyclerViewOverlay()
            captureScreenshot()
            setUpSearchBtnOverlay()
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    startMediaProjection()
//            } else {
//                captureLegacyScreenshot()
//            }
        }
    }

    private fun setupMediaProjection() {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        mediaProjection?.apply {
            registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    virtualDisplay?.release()
                    virtualDisplay = null
                    imageReader?.close()
                    imageReader = null
                    mediaProjection = null
//                    Toast.makeText(this@OverlaySearchService, "MediaProjection stopped", Toast.LENGTH_SHORT).show()
                }
            }, null)

            virtualDisplay = createVirtualDisplay(
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmapToStorage(bitmap)
        } else {
            saveBitmapToStorageLegacy(bitmap)
        }
    }

    private fun saveBitmapToStorageLegacy(bitmap: Bitmap) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "Screenshot_${System.currentTimeMillis()}.png"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")

            // Use RELATIVE_PATH for Android 10 (API 29) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
            } else {
                // For Android 9 and below, provide the absolute file path
                val screenshotsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Screenshots"
                val screenshotsDirFile = File(screenshotsDir)
                if (!screenshotsDirFile.exists()) {
                    screenshotsDirFile.mkdirs() // Create the directory if it doesn't exist
                }
                put(
                    MediaStore.Images.Media.DATA,
                    "$screenshotsDir/Screenshot_${System.currentTimeMillis()}.png"
                )
            }
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
            Toast.makeText(this, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "Screenshot_${System.currentTimeMillis()}.png"
            )
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
//                Toast.makeText(this, "Failed to save screenshot: ${e.message}", Toast.LENGTH_SHORT)
//                    .show()
            }
        } else {
            Toast.makeText(this, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to open a URL in the device's browser
    private fun openUrl(url: String) {
        try {
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", url)  // Pass the URL to the WebViewActivity
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK  // Add this flag when starting a new activity from a service
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(
                "Error Exception", e.message.toString()
            )
//            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchItemsFromApi(imageUri: Uri) {
        progressBar.visibility = View.VISIBLE
        txtMessage.visibility = View.GONE
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
                        txtMessage.visibility = View.VISIBLE
                        txtMessage.text = "Failed to fetch items"
//                        Toast.makeText(applicationContext, "Failed to fetch items", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OverlayListResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    txtMessage.visibility = View.VISIBLE
                    txtMessage.text = "${t.message}"
//                    Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            txtMessage.visibility = View.VISIBLE
            txtMessage.text = "${e.message}"
//            Toast.makeText(this, "Failed to prepare file: ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun setupRecyclerViewOverlay() {
        recyclerViewOverlay = LayoutInflater.from(this).inflate(R.layout.overlay_list_screen, null)
        webView = recyclerViewOverlay.findViewById(R.id.webView)
        progressBar = recyclerViewOverlay.findViewById(R.id.progressBar)
        txtMessage = recyclerViewOverlay.findViewById(R.id.txtMessage)
        val imgClose = recyclerViewOverlay.findViewById<AppCompatImageView>(R.id.img_close)
        val imgBack = recyclerViewOverlay.findViewById<AppCompatImageView>(R.id.img_back)
        val recyclerList = recyclerViewOverlay.findViewById<FrameLayout>(R.id.recyclerList)
        imgClose.setOnClickListener {
            hideRecyclerViewOverlay()
        }
        imgBack.setOnClickListener {
            if (webView.canGoBack()) {
                // If the WebView can go back in history, navigate back in the WebView
                webView.goBack()
            } else {
                imgBack.visibility = View.GONE
                webView.visibility = View.GONE
                recyclerList.visibility = View.VISIBLE
            }
        }
        // RecyclerView setup
        recyclerView = recyclerViewOverlay.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OverlayListAdapter(listOf()) { item ->
//            openUrl(item.url)
//            hideRecyclerViewOverlay()

            //todo: Changes for Review
            imgBack.visibility = View.VISIBLE
            webView.visibility = View.VISIBLE
            recyclerList.visibility = View.GONE
            webView.loadUrl(item.url)

        }
        recyclerView.adapter = adapter


        webView.webViewClient = WebViewClient()  // To open URLs within the WebView

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
//                webView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
//                webView.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                when {
                    url.startsWith("http://") || url.startsWith("https://") -> {
                        // Let WebView load the URL
                        return false
                    }
                    url.startsWith("mailto:") -> {
                        // Handle mailto: scheme
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                    url.startsWith("tel:") -> {
                        // Handle tel: scheme
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                    else -> {
                        // Handle other custom schemes if needed
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            return true
                        } catch (e: Exception) {
                            Log.e("Exception in Webview", e.message.toString())
                            // Show error or handle accordingly
//                            Toast.makeText(this@WebViewActivity, "Unable to handle this URL scheme.", Toast.LENGTH_SHORT).show()
                            return true
                        }
                    }
                }
            }
        }

        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        // Clear previous cache and load new URL
        webView.clearCache(true)
        webView.clearHistory()

//        // Layout parameters for the RecyclerView overlay
//        val recyclerParams = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            PixelFormat.TRANSLUCENT
//        )
//        recyclerParams.gravity = Gravity.CENTER
    }

    private fun showRecyclerViewOverlay(uri: Uri) {
// Dynamically calculate screen width and height
        val displayMetrics = DisplayMetrics()
        val display = windowManager.defaultDisplay
        display.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val recyclerParams = WindowManager.LayoutParams(
            (screenWidth * 0.7).toInt(),
            (screenHeight * 0.5).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        recyclerParams.gravity = Gravity.END

        if (recyclerViewOverlay.parent != null) {
            // Avoid adding the same view again
            windowManager.removeView(recyclerViewOverlay)
        }

        windowManager.addView(recyclerViewOverlay, recyclerParams)
        isRecyclerViewVisible = true

        fetchItemsFromApi(uri)
    }

    private fun hideRecyclerViewOverlay() {
//        if (isRecyclerViewVisible) {
            windowManager.removeView(recyclerViewOverlay)
            isRecyclerViewVisible = false
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlaySearchBtnView.isInitialized) windowManager.removeView(overlaySearchBtnView)
        if (isRecyclerViewVisible) windowManager.removeView(recyclerViewOverlay)
//        if (::cancelView.isInitialized) windowManager.removeView(cancelView)
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "overlay_service_channel"
        val channelName = "Overlay Service"

        // Create Notification Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Create the notification
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Overlay Service")
            .setContentText("The service is running to enable overlay functionality.")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Start the service in the foreground
        startForeground(1, notification)
    }
}