package com.overlayscreendesigntest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overlayscreendesigntest.component.PreferenceManager
import com.overlayscreendesigntest.screens.HomeActivity
import com.overlayscreendesigntest.screens.LoginActivity
import com.overlayscreendesigntest.screens.SignUpActivity
import com.overlayscreendesigntest.ui.theme.ShoptainmentAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        // Check if the user is logged in
        if (preferenceManager.isLoggedIn()) {
            // Redirect to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()  // Close MainActivity
        }
        setContent {
            ShoptainmentAppTheme {
                ShopAppScreen(onLoginClick = {
                    // Start the LoginActivity when the Login button is clicked
                    startActivity(Intent(this, LoginActivity::class.java))
                }, onSignUpClick = {
                    // Start the LoginActivity when the Login button is clicked
                    startActivity(Intent(this, SignUpActivity::class.java))
                })
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun ShopAppScreen(onLoginClick: () -> Unit, onSignUpClick: () -> Unit) {
    // Background color

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xff1B63AF), // Dark blue
                        Color(0xff0B2846)  // Lighter blue
                    )
                )
            )
    // Set your custom blue background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Adjust to space things vertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.illustration),
                contentDescription = "Profile 2",
                modifier = Modifier
                    .fillMaxWidth().height(screenHeight/3), // Image fills the available width
                contentScale = ContentScale.Fit
            )

            // Welcome text
            Column(
                modifier = Modifier.padding(bottom = 100.dp)
            ) {
                Text(
                    text = "Welcome to",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Shoptainment",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Interact with video content to explore and purchase items featured in the video.",
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 22.sp // Line spacing
                )
            }

            // Buttons at the bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onLoginClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFffffff),
                            contentColor = Color(0xff1B63AF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onSignUpClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFffffff),
                            contentColor = Color(0xff1B63AF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(
                            text = "Sign up",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login as guest Button with outline
                OutlinedButton(

                    onClick = {  },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "Login as guest",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}