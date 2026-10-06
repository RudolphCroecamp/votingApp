package com.example.frontend.ui.Registration.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun CreateAccountScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationUiEvent) -> Unit,
    onNextClick: () -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold) },
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
            RegistrationStepper(currentStep = 1)

            Text(
                text = "Let's create your account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter your details to register as a voter.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.idNumber,
                onValueChange = { onEvent(RegistrationUiEvent.OnIdNumberChanged(it)) },
                label = { Text("ID / Passport Number") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.fullNames,
                onValueChange = { onEvent(RegistrationUiEvent.OnFullNamesChanged(it)) },
                label = { Text("Full Names") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            //#TODO: Swap dob met postal code
            //order:
            //1. name
            //id
            //cell
            //email
            //postal
            OutlinedTextField(
                value = uiState.dob,
                onValueChange = { onEvent(RegistrationUiEvent.OnDobChanged(it)) },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { onEvent(RegistrationUiEvent.OnPhoneChanged(it)) },
                label = { Text("Cellphone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { onEvent(RegistrationUiEvent.OnEmailChanged(it)) },
                label = { Text("Email Address (Optional)") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your information is encrypted and used only to verify your identity and eligibility to vote.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2D59))
            ) {
                Text("Continue  →", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", fontSize = 12.sp, color = Color(0xFF64748B))
                TextButton(
                    onClick = onSignInClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Preview(name = "Create Account - Empty", showBackground = true, showSystemUi = true)
@Composable
private fun CreateAccountScreenEmptyPreview() {
    FrontendTheme {
        CreateAccountScreen(
            uiState = RegistrationUiState(),
            onEvent = {},
            onNextClick = {},
            onSignInClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Create Account - Filled", showBackground = true, showSystemUi = true)
@Composable
private fun CreateAccountScreenFilledPreview() {
    FrontendTheme {
        CreateAccountScreen(
            uiState = RegistrationUiState(
                idNumber = "0203015800085",
                fullNames = "Adriaan van Wyk",
                dob = "2002-03-01",
                phone = "0712345678",
                email = "adriaan@example.com"
            ),
            onEvent = {},
            onNextClick = {},
            onSignInClick = {},
            onBackClick = {}
        )
    }
}
