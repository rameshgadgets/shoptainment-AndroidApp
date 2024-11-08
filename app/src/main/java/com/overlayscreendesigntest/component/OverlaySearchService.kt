package com.overlayscreendesigntest.component

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(1, createNotification())
        } else {
            startForeground(
                1, createNotification(),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
        startForeground(1, createNotification())

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

            if (isRecyclerViewVisible) {
                hideRecyclerViewOverlay()
            } else {
                showRecyclerViewOverlay()
            }
        }

        fetchItemsFromApi()
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

    private fun fetchItemsFromApi() {
        progressBar.visibility = View.VISIBLE
        val call = RetrofitClient.api.fetchOverLayScreenItems(
            apiKey = "08dfd65b433cbda8df28127070d9875cf3450203ecfc87939f579949aa3c1be7",
            catalogName = "Lykdat",
            imageUrl = "https://img.shopstyle-cdn.com/pim/b1/78/b178242102a268552b4a232af741dc9c_best.jpg"
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
                    adapter.updateItems(allSimilarProduct)
                } else {
                    Toast.makeText(applicationContext, "Failed to fetch items", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<OverlayListResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
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

    private fun showRecyclerViewOverlay() {
        if (!isRecyclerViewVisible) {
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
        }
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

    private fun takeScreenshotAndSave(context: Context, onSaved: (Uri) -> Unit) {
        val rootView = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        rootView?.let {
            // Take the screenshot as per the requirements
            // Here, use actual view capturing logic depending on your specific requirement
            val file = File(context.getExternalFilesDir(null), "screenshot.png")
            FileOutputStream(file).use { out ->
                // Your screenshot logic here
                onSaved(Uri.fromFile(file))
            }
        }
    }
}