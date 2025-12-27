package com.it2161.s231292a.movieviewer.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.it2161.s231292a.movieviewer.R
import com.it2161.s231292a.movieviewer.ui.components.AppHeader
import com.it2161.s231292a.movieviewer.ui.components.CameraCaptureDialog
import com.it2161.s231292a.movieviewer.ui.components.DatePickerInput
import com.it2161.s231292a.movieviewer.ui.components.PasswordInput
import com.it2161.s231292a.movieviewer.ui.components.TextInput
import com.it2161.s231292a.movieviewer.ui.models.RegisterViewModel
import java.io.File

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showCamera by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Create Account",
                canNavigateBack = true,
                onNavigateBack = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.profilePicturePath != null) {
                    AsyncImage(
                        model = File(uiState.profilePicturePath!!),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Default Avatar",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Camera button overlay
                IconButton(
                    onClick = { showCamera = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap camera to take a profile picture",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User ID Field
            TextInput(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = "User ID",
                placeholder = "Enter a unique user ID",
                errorText = uiState.usernameError,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            PasswordInput(
                password = uiState.password,
                onPasswordChange = viewModel::updatePassword,
                passwordVisible = uiState.passwordVisible,
                onPasswordVisibilityChange = { viewModel.togglePasswordVisibility() },
                errorText = uiState.passwordError,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            PasswordInput(
                label = "Confirm Password",
                placeholder = "Re-enter your password",
                password = uiState.confirmPassword,
                onPasswordChange = viewModel::updateConfirmPassword,
                passwordVisible = uiState.confirmPasswordVisible,
                onPasswordVisibilityChange = { viewModel.toggleConfirmPasswordVisibility() },
                errorText = uiState.confirmPasswordError,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred Name Field
            TextInput(
                value = uiState.preferredName,
                onValueChange = viewModel::updatePreferredName,
                label = "Preferred Name",
                placeholder = "What should we call you?",
                errorText = uiState.preferredNameError,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth Field
            DatePickerInput(
                label = "Date of Birth",
                selectedDate = uiState.dateOfBirth,
                onDateSelected = viewModel::updateDateOfBirth,
                showPicker = uiState.showDatePicker,
                onShowPicker = viewModel::showDatePicker,
                onDismiss = viewModel::hideDatePicker,
                errorText = uiState.dateOfBirthError,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    viewModel.register(
                        onSuccess = {
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }
        }
    }

    // Camera Dialog
    if (showCamera) {
        CameraCaptureDialog(
            onImageCaptured = { imageBytes ->
                val path = viewModel.saveProfilePicture(context, imageBytes)
                viewModel.setProfilePicturePath(path)
            },
            onDismiss = { showCamera = false }
        )
    }
}

