package com.example.frontend.ui.Registration.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.Registration.components.RegistrationStepper
import com.example.frontend.ui.theme.FrontendTheme

@Composable
fun RegistrationCompleteScreen(
    onGoToSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            RegistrationStepper(currentStep = 4)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF16A34A), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registration Successful!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your account has been created successfully.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Confirmation Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "You are now registered and ready to vote.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "You can sign in to your account and cast your vote when voting opens.",
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Important Information",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoBullet(
                icon = Icons.Default.Lock,
                title = "Your privacy is protected",
                subtitle = "Your identity is secure and your vote will remain anonymous."
            )
            InfoBullet(
                icon = Icons.Default.Shield,
                title = "One person. One vote.",
                subtitle = "You can only vote once in each election."
            )
        }

        // Button to proceed to Sign In
        Button(
            onClick = onGoToSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
        ) {
            Text("Go to Sign In  →", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoBullet(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0F2D59), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}
@Preview(name = "Registration Complete", showBackground = true, showSystemUi = true)
@Composable
private fun RegistrationCompleteScreenPreview() {
    FrontendTheme {
        RegistrationCompleteScreen(onGoToSignInClick = {})
    }
}

@Preview(name = "Info Bullet", showBackground = true, widthDp = 320)
@Composable
private fun InfoBulletPreview() {
    FrontendTheme {
        InfoBullet(
            icon = Icons.Default.Lock,
            title = "Your privacy is protected",
            subtitle = "Your identity is secure and your vote will remain anonymous."
        )
    }
}
