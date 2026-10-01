package com.example.frontend.ui.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.example.frontend.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.FrontendTheme

@Composable
fun SuccessCheckmarkBadge (){
   Box(
       modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF0F8A4B)),
       contentAlignment = Alignment.Center
   ){
       Image(
           painter = painterResource(R.drawable.success_check),
           contentDescription = "Success checkmark",
           contentScale = ContentScale.Crop,
           modifier = Modifier.fillMaxSize().clip(CircleShape)
       )
   }
}
