@file:OptIn(ExperimentalMaterial3Api::class)

package com.overlayscreendesigntest.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.overlayscreendesigntest.R
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController, drawerState: DrawerState) {
    // State to toggle password visibility
    val passwordVisibility = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    var nameFocused by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Sanjay Sharma") }
    var email by remember { mutableStateOf("sanjaysharma@gmail.com") }
    var password by remember { mutableStateOf("12344556") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
//                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
        ) {
            // Profile Picture Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable {
//                            openDrawerInHome()
                            coroutineScope.launch { drawerState.open() }
                                   },
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
                    text = "Profile",
                    fontSize = 20.sp,
                    color = Color.Black
                )

                // Notification Icon
                Box(
                    modifier = Modifier.clickable { navController.navigate("notification") },
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
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
//                    .size(120.dp)
                    .clickable { /* Handle profile picture click */ },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.camera_placeholder), // Replace with your actual icon
                    contentDescription = "Add Profile Picture",
                    modifier = Modifier.size(102.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Add Profile Picture", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Name TextField
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter your name") },
//                leadingIcon = {
//                    Icon(
//                        Icons.Default.Email, contentDescription = null,
//                        tint = if (emailFocused) Color.Black else Color(0xffcccccc)
//                    )
//                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onFocusChanged { nameFocused = it.isFocused },
                shape = RoundedCornerShape(10.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedLabelColor = Color.Black, // Focused hint text color
//                    unfocusedLabelColor = Color(0xffcccccc), // Unfocused hint text color
//                    focusedBorderColor = Color.Black,
//                    unfocusedBorderColor = Color(0xffcccccc)
//                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
//                    keyboardActions = KeyboardActions(
//                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
//                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
//                leadingIcon = {
//                    Icon(
//                        Icons.Default.Email, contentDescription = null,
//                        tint = if (emailFocused) Color.Black else Color(0xffcccccc)
//                    )
//                },
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 16.dp)
                    .onFocusChanged { emailFocused = it.isFocused },
                shape = RoundedCornerShape(10.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedLabelColor = Color.Black, // Focused hint text color
//                    unfocusedLabelColor = Color(0xffcccccc), // Unfocused hint text color
//                    focusedBorderColor = Color.Black,
//                    unfocusedBorderColor = Color(0xffcccccc)
//                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField with visibility toggle
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
//                leadingIcon = {
//                    Icon(
//                        Icons.Default.Lock,
//                        contentDescription = null,
//                        tint = if (passwordFocused) Color.Black else Color(0xffcccccc)
//                    )
//                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Image(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.hide else R.drawable.show
                            ), // Replace with your visibility/visibility off PNGs
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color(0xffcccccc))
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 16.dp)
                    .onFocusChanged { passwordFocused = it.isFocused },
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedLabelColor = Color.Black, // Focused hint text color
//                    unfocusedLabelColor = Color(0xffcccccc), // Unfocused hint text color
//                    focusedBorderColor = Color.Black,
//                    unfocusedBorderColor = Color(0xffcccccc)
//                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Aligns the buttons at the bottom of the screen
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .size(174.dp, 86.dp)
                    .background(Color.White)
                    .border(0.2.dp, Color.Gray, RoundedCornerShape(20.dp)).clickable { navController.navigate("home") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.home), // Replace with your actual drawable
                        contentDescription = "Home Icon",
                        tint = colorResource(id = R.color.blue),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Home",
                        color = colorResource(id = R.color.blue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Profile Button
            Box(
                modifier = Modifier
                    .size(174.dp, 86.dp)
                    .background(Color(0xff1B63AF), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile), // Replace with your actual drawable
                        contentDescription = "Profile Icon",
                        tint = Color(0xFFFFFFFF), // Match the background color of the top bar
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Profile",
                        color = Color(0xFFFFFFFF), // Match the blue color
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    ProfileScreen(navController = rememberNavController())
//}