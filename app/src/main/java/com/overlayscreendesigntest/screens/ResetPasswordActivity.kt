package com.overlayscreendesigntest.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overlayscreendesigntest.R

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResetPasswordScreen(onBackClick = {
                finish()
            }, onConfirmClick = {
                startActivity(Intent(this, PasswordChangedActivity::class.java))
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(onBackClick: () -> Unit, onConfirmClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { padding ->
            var newPassword by remember { mutableStateOf("") }
            var reEnterPassword by remember { mutableStateOf("") }
            var newPasswordFocused by remember { mutableStateOf(false) }
            var reEnterPasswordFocused by remember { mutableStateOf(false) }
            var newPasswordVisible by remember { mutableStateOf(false) }
            var reEnterPasswordVisible by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 20.dp, start = 30.dp, end = 30.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Reset Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "Create a new password. Ensure it differs from previous ones for security",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (newPasswordFocused) Color.Black else Color(0xffcccccc)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Image(
                                painter = painterResource(
                                    id = if (newPasswordVisible) R.drawable.hide else R.drawable.show
                                ), // Replace with your visibility/visibility off PNGs
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color(0xffcccccc))
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { newPasswordFocused = it.isFocused },
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = PasswordVisualTransformation(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedLabelColor = Color.Black, // Focused hint text color
//                        unfocusedLabelColor = Color(0xffcccccc), // Unfocused hint text color
//                        focusedBorderColor = Color.Black,
//                        unfocusedBorderColor = Color(0xffcccccc)
//                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
//                    keyboardActions = KeyboardActions(
//                        onNext = { focusManager.moveFocus(FocusDirection.Exit) }
//                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reEnterPassword,
                    onValueChange = { reEnterPassword = it },
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (reEnterPasswordFocused) Color.Black else Color(0xffcccccc)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { reEnterPasswordVisible = !reEnterPasswordVisible }) {
                            Image(
                                painter = painterResource(
                                    id = if (reEnterPasswordVisible) R.drawable.hide else R.drawable.show
                                ), // Replace with your visibility/visibility off PNGs
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color(0xffcccccc))
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { reEnterPasswordFocused = it.isFocused },
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = PasswordVisualTransformation(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedLabelColor = Color.Black, // Focused hint text color
//                        unfocusedLabelColor = Color(0xffcccccc), // Unfocused hint text color
//                        focusedBorderColor = Color.Black,
//                        unfocusedBorderColor = Color(0xffcccccc)
//                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
//                    keyboardActions = KeyboardActions(
//                        onNext = { focusManager.moveFocus(FocusDirection.Exit) }
//                    )
                )
                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.blue))
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}