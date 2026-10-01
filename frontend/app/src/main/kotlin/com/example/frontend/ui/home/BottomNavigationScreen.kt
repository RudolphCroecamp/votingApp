package com.example.frontend.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.ui.theme.FrontendTheme

@Composable
fun BottomNavigationScreen (modifier: Modifier = Modifier){

}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationScreenPreview(){
    FrontendTheme() {
        BottomNavigationScreen()
    }
}