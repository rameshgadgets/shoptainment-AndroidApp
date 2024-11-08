package com.overlayscreendesigntest.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.overlayscreendesigntest.R

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the URL from the Intent
        val url = intent.getStringExtra("url") ?: return

        Log.e("URL WEBVIEW", url)

        // Initialize the WebView and load the URL
        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()  // To open URLs within the WebView

        webView.webViewClient = object : WebViewClient() {
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

        webView.settings.javaScriptEnabled = true  // Enable JavaScript if needed
        webView.loadUrl(url)

        // Set up the back icon functionality
        val backIcon: AppCompatImageView = findViewById(R.id.back_icon)
        backIcon.setOnClickListener {
            if (webView.canGoBack()) {
                // If the WebView can go back in history, navigate back in the WebView
                webView.goBack()
            } else {
                // If WebView cannot go back, navigate to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()  // Finish WebViewActivity to prevent it from being on the backstack
            }
        }
    }

    override fun onBackPressed() {
        handleBackNavigation()
    }

    private fun handleBackNavigation() {
        if (webView.canGoBack()) {
            // If the WebView can go back in history, navigate back in the WebView
            webView.goBack()
        } else {
            // If WebView cannot go back, navigate to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()  // Finish WebViewActivity to prevent it from being on the backstack
        }
    }
}