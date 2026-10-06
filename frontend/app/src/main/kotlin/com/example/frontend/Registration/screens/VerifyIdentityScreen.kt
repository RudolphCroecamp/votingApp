package com.example.frontend.ui.Registration.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.Registration.components.RegistrationStepper
import com.example.frontend.ui.Registration.IdentityDocumentType
import com.example.frontend.ui.Registration.RegistrationUiEvent
import com.example.frontend.ui.Registration.RegistrationUiState
import com.example.frontend.ui.theme.FrontendTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyIdentityScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Identity", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp)
        ) {
            RegistrationStepper(currentStep = 2)

            Text(
                text = "Identity Document Verification",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "Select your document type and capture a clear photo for identity verification.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ID Document Type Selector Options
            Text("Document Type", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DocumentOptionChip(
                    label = "National ID",
                    isSelected = uiState.documentType == IdentityDocumentType.NATIONAL_ID,
                    onClick = { onEvent(RegistrationUiEvent.OnDocumentTypeSelected(IdentityDocumentType.NATIONAL_ID)) },
                    modifier = Modifier.weight(1f)
                )

                DocumentOptionChip(
                    label = "Passport",
                    isSelected = uiState.documentType == IdentityDocumentType.PASSPORT,
                    onClick = { onEvent(RegistrationUiEvent.OnDocumentTypeSelected(IdentityDocumentType.PASSPORT)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.documentNumber,
                onValueChange = { onEvent(RegistrationUiEvent.OnDocumentNumberChanged(it)) },
                label = {
                    Text(
                        if (uiState.documentType == IdentityDocumentType.NATIONAL_ID) "Enter ID Number"
                        else "Enter Passport Number"
                    )
                },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            //A Photo Capture Container
            Text("Document Photo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFCBD5E1), shape = RoundedCornerShape(12.dp))
                    .clickable {
                        // Simulate capturing document photo
                        onEvent(RegistrationUiEvent.OnDocumentCaptured("path/to/captured_image.jpg"))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.documentFrontImagePath != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Document Captured Successfully", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF0F2D59), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to capture front of document", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // The Submit / Store Button
            Button(
                onClick = {
                    onEvent(RegistrationUiEvent.OnSaveIdentityDetails)
                    onNextClick()
                },
                enabled = uiState.documentNumber.isNotBlank() && uiState.documentFrontImagePath != null && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2D59))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save & Verify Identity  →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DocumentOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF0F2D59) else Color(0xFFE2E8F0)
    val bgColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
    val textColor = if (isSelected) Color(0xFF0F2D59) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
@Preview(name = "Verify Identity - Empty", showBackground = true, showSystemUi = true)
@Composable
private fun VerifyIdentityScreenEmptyPreview() {
    FrontendTheme {
        VerifyIdentityScreen(
            uiState = RegistrationUiState(),
            onEvent = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Verify Identity - Passport Captured", showBackground = true, showSystemUi = true)
@Composable
private fun VerifyIdentityScreenCapturedPreview() {
    FrontendTheme {
        VerifyIdentityScreen(
            uiState = RegistrationUiState(
                documentType = IdentityDocumentType.PASSPORT,
                documentNumber = "A01234567",
                documentFrontImagePath = "path/to/captured_image.jpg"
            ),
            onEvent = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Verify Identity - Loading", showBackground = true, showSystemUi = true)
@Composable
private fun VerifyIdentityScreenLoadingPreview() {
    FrontendTheme {
        VerifyIdentityScreen(
            uiState = RegistrationUiState(
                documentNumber = "0203015800085",
                documentFrontImagePath = "path/to/captured_image.jpg",
                isLoading = true
            ),
            onEvent = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Document Chip", showBackground = true, widthDp = 200)
@Composable
private fun DocumentOptionChipPreview() {
    FrontendTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DocumentOptionChip(label = "National ID", isSelected = true, onClick = {})
            DocumentOptionChip(label = "Passport", isSelected = false, onClick = {})
        }
    }
}
