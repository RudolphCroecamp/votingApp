package com.example.frontend.ui.Registration.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.Registration.components.RegistrationStepper
import com.example.frontend.ui.Registration.RegistrationUiEvent
import com.example.frontend.ui.Registration.RegistrationUiState
import com.example.frontend.ui.theme.FrontendTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceVerificationScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Verification", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F2D59))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegistrationStepper(currentStep = 3)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Biometric Verification",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Position your face in the oval frame and capture a live photo to confirm your identity.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Oval Camera Frame / Viewport Preview
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 280.dp)
                    .clip(RoundedCornerShape(110.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(
                        width = 3.dp,
                        color = if (uiState.capturedFaceImagePath != null) Color(0xFF16A34A) else Color(0xFF0F2D59),
                        shape = RoundedCornerShape(110.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.capturedFaceImagePath != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Face Captured",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Photo Captured",
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Face Oval Placeholder",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Align Face Here",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            //Capture Photo Trigger Button
            IconButton(
                onClick = {
                    // Simulates capturing image file path from camera preview/CameraX
                    onEvent(RegistrationUiEvent.OnFaceCaptured("path/to/captured_face.jpg"))
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF0F2D59), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Take Photo",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (uiState.capturedFaceImagePath != null) "Retake Photo" else "Tap to Capture",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Verify & Continue Button
            Button(
                onClick = {
                    onEvent(RegistrationUiEvent.OnVerifyFaceSubmitted)
                    onNextClick()
                },
                enabled = uiState.capturedFaceImagePath != null && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2D59))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify & Continue  →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Preview(name = "Face Verification - Not Captured", showBackground = true, showSystemUi = true)
@Composable
private fun FaceVerificationScreenEmptyPreview() {
    FrontendTheme {
        FaceVerificationScreen(
            uiState = RegistrationUiState(),
            onEvent = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Face Verification - Captured", showBackground = true, showSystemUi = true)
@Composable
private fun FaceVerificationScreenCapturedPreview() {
    FrontendTheme {
        FaceVerificationScreen(
            uiState = RegistrationUiState(
                capturedFaceImagePath = "path/to/captured_face.jpg"
            ),
            onEvent = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Face Verification - Loading", showBackground = true, showSystemUi = true)
@Composable
private fun FaceVerificationScreenLoadingPreview() {
    FrontendTheme {
        FaceVerificationScreen(
            uiState = RegistrationUiState(
                capturedFaceImagePath = "path/to/captured_face.jpg",
                isLoading = true
            ),
            onEvent = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}
