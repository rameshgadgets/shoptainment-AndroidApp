package com.overlayscreendesigntest.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overlayscreendesigntest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.provider.Settings
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.overlayscreendesigntest.component.OverlaySearchService
import com.overlayscreendesigntest.component.PreferenceManager

class HomeActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        setContent {
            DrawerWithHomeScreenBox(
                this,
                preferenceManager
            )
        }
    }

    // Function to check if overlay permission is granted
    fun hasOverlayPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    // Request overlay permission by opening the settings screen
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    // Activity result launcher to handle permission result
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasOverlayPermission(this)) {
            Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied, show a message to the user
            Toast.makeText(this, "Overlay permission is required.", Toast.LENGTH_SHORT).show()
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun DrawerWithHomeScreenBox(
    context: Context,
    preferenceManager: PreferenceManager
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerOpen = remember { mutableStateOf(false) }  // State for drawer open/close
    var isPowerOn by remember { mutableStateOf(false) }   // State for Power button actions
    var selectedItem by remember { mutableStateOf("About Us") }
    var isServiceRunning by remember { mutableStateOf(preferenceManager.isServiceRunning()) }

    val navController = rememberNavController()
    DisposableEffect(key1 = true) {
        val lifecycleOwner = ProcessLifecycleOwner.get()
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                // When app comes to the foreground, check the state
                isServiceRunning = preferenceManager.isServiceRunning()
                if (isServiceRunning) {
                    isPowerOn = true
                    startOverlayService(context, preferenceManager)
                } else {
                    isPowerOn = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // Cleanup observer when the composable is disposed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                selectedItem = selectedItem,
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close() // Close the drawer after selection
                    }
                }, onItemSelected = { selectedItem = it })
        },
        content = {
            NavHost(navController, startDestination = "home") {
                composable("home") {
                    ShoptainmentScreen(isPowerOn = isPowerOn,
                        onPowerButtonClick = {
                            if (!(context as HomeActivity).hasOverlayPermission(context)) {
                                context.requestOverlayPermission()
                            } else {
                                isPowerOn = !isPowerOn
                                if (isPowerOn) {
                                    startOverlayService(context, preferenceManager)
                                    context.moveTaskToBack(true)
//                                onPowerBtnClick()
                                } else {
                                    stopOverlayService(context, preferenceManager)
                                }
                            }
                        },
                        onMenuClick = { drawerOpen.value = true },
                        scope = scope,
                        drawerState = drawerState,
                        onHomeClick = {
                            navController.navigate("home")
                        },
                        onProfileClick = {
                            navController.navigate("profile")
                        },
                        onNotificationClick = {
                            navController.navigate("notification")
                        })
                }
                composable("profile") {
                    ProfileScreen(navController, drawerState)
                }
                composable("notification") {
                    NotificationScreen(navController, drawerState)
                }
            }
        }
    )
}

private fun startOverlayService(context: Context, preferenceManager: PreferenceManager) {
    val intent = Intent(context, OverlaySearchService::class.java)
    ContextCompat.startForegroundService(context, intent)

    preferenceManager.setServiceRunning(true)
    preferenceManager.setOverlayVisible(true)
}

private fun stopOverlayService(context: Context, preferenceManager: PreferenceManager) {
    val intent = Intent(context, OverlaySearchService::class.java)
    context.stopService(intent)

    // Save state in SharedPreferences
    preferenceManager.setServiceRunning(false)
    preferenceManager.setOverlayVisible(false)
}

@Composable
fun DrawerContent(
    selectedItem: String,
    onCloseDrawer: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xff1B63AF), // Dark blue
                        Color(0xff0B2846)  // Lighter blue
                    )
                ), RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp)
            )
            .padding(start = 25.dp, top = 60.dp)
    ) {
        // Drawer Header
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Logo",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Drawer Menu Items
        DrawerItem(
            text = "About Us",
            icon = painterResource(id = R.drawable.about_us),
            onClick = {
                onItemSelected("About Us")
                onCloseDrawer()
            },
            isSelected = selectedItem == "About Us"
        )
        DrawerItem(
            text = "Privacy Policy",
            icon = painterResource(id = R.drawable.privacy),
            onClick = {
                onItemSelected("Privacy Policy")
                onCloseDrawer()
            },
            isSelected = selectedItem == "Privacy Policy"
        )
        DrawerItem(
            text = "Feedback Details",
            icon = painterResource(id = R.drawable.feedback),
            onClick = {
                onItemSelected("Feedback Details")
                onCloseDrawer()
            },
            isSelected = selectedItem == "Feedback Details"
        )
        DrawerItem(
            text = "Terms & Conditions",
            icon = painterResource(id = R.drawable.terms),
            onClick = {
                onItemSelected("Terms & Conditions")
                onCloseDrawer()
            },
            isSelected = selectedItem == "Terms & Conditions"
        )
        DrawerItem(
            text = "Settings",
            icon = painterResource(id = R.drawable.settings),
            onClick = {
                onItemSelected("Settings")
                onCloseDrawer()
            },
            isSelected = selectedItem == "Settings"
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.padding(PaddingValues(bottom = 40.dp, start = 35.dp))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(25) // Rounded corners
                    )
                    .clickable { onCloseDrawer() }
                    .padding(horizontal = 24.dp, vertical = 12.dp) // Padding around the content
                    .wrapContentWidth() // Adjusts the width to the content
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logout), // Replace with your image resource
                    contentDescription = "Logout",
                    modifier = Modifier.size(24.dp) // Adjust the size as per your image
                )
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                Text(
                    text = "Logout",
                    color = contentColor, // Blue text color
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DrawerItem(text: String, icon: Painter, isSelected: Boolean = false, onClick: () -> Unit) {

    // Conditional background color based on the selection state
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF7291F4) else Color.White
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(top = 6.dp, bottom = 6.dp, start = 10.dp))
            .background(
                color = backgroundColor, // Highlight selected item
                shape = RoundedCornerShape(
                    topStart = 25.dp,
                    bottomStart = 25.dp
                )  // Rounded corners for selected item
            )
            .padding(PaddingValues(top = 14.dp, start = 20.dp, bottom = 14.dp))
//            .padding(vertical = 20.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = icon,
            modifier = Modifier
                .width(24.dp)
                .height(24.dp),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (isSelected) Color(0xFF1565C0) else Color.White),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
fun ShoptainmentScreen(
    scope: CoroutineScope,
    drawerState: DrawerState,
    isPowerOn: Boolean,
    onPowerButtonClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Modifier to set the background color conditionally

    Scaffold { paddingValues ->
        val backgroundModifier = if (isPowerOn) {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White) // Set background to white when power is on
        } else {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xff1B63AF), // Dark blue
                            Color(0xff0B2846)  // Lighter blue
                        )
                    ) // Set gradient when power is off
                )
        }
        Box(
            modifier = backgroundModifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButtonBar(onNotificationClick, scope, drawerState)
                Spacer(modifier = Modifier.weight(1f))
                PowerButton(onPowerButtonClick, isPowerOn)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isPowerOn) "Tap to Stop.." else "Tap to Start..",
                    color = if (isPowerOn) colorResource(id = R.color.blue) else Color.White,
                    fontSize = 20.sp
                )
                if (isPowerOn) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GreenIndicator()
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connected",
                            color = Color(0xFF0073FF),
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                BottomNavigationBar(isPowerOn, onHomeClick, onProfileClick)
            }
        }
    }
}

@Composable
fun GreenIndicator() {
    Box(
        modifier = Modifier
            .size(12.dp) // Size of the indicator
            .clip(CircleShape) // Make it circular
            .background(Color(0xFF00FF00)) // Green color
    )
}

@Composable
fun PowerButton(onPowerButtonClick: () -> Unit, isPowerOn: Boolean) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .clickable { onPowerButtonClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = if (isPowerOn) R.drawable.btn_stop else R.drawable.btn_start), // Using PNG resource
            contentDescription = "Power Button",
            modifier = Modifier.size(300.dp)
        )
    }
}

@Composable
fun IconButtonBar(
    onNotificationClick: () -> Unit,
    scope: CoroutineScope,
    drawerState: DrawerState,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickable { scope.launch { drawerState.open() } },
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(52.dp),
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Menu"
            )
        }

        // App Title
        Text(
            text = "Shoptainment",
            fontSize = 20.sp,
            color = Color.White
        )

        // Notification Icon
        Box(
            modifier = Modifier.clickable { onNotificationClick() },
//                .size(48.dp)
//                .background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(52.dp),
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notification"
            )
        }
    }
}

@Composable
fun BottomNavigationBar(isPowerOn: Boolean, onHomeClick: () -> Unit, onProfileClick: () -> Unit) {
    val homeButtonModifier = if (isPowerOn) {
        Modifier
            .size(174.dp, 86.dp)
            .background(Color(0xff1B63AF), RoundedCornerShape(20.dp))
    } else {
        Modifier
            .size(174.dp, 86.dp)
            .border(
                BorderStroke(1.dp, Color.White),
                shape = RoundedCornerShape(20.dp)
            )
    }

    val profileButtonModifier = if (isPowerOn) {
        Modifier
            .size(174.dp, 86.dp)
            .background(Color.White)
            .border(0.2.dp, Color.Gray, RoundedCornerShape(20.dp))
    } else {
        Modifier
            .size(174.dp, 86.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = homeButtonModifier.clickable { onHomeClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.home), // Replace with your actual drawable
                    contentDescription = "Home Icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Home",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile Button
        Box(
            modifier = profileButtonModifier.clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.profile), // Replace with your actual drawable
                    contentDescription = "Profile Icon",
                    tint = Color(0xFF5A8DDF), // Match the background color of the top bar
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Profile",
                    color = Color(0xFF5A8DDF), // Match the blue color
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}