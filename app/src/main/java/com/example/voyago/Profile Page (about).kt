package com.example.voyago

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MyProfile(modifier: Modifier = Modifier) {
    var sliderPosition by remember { mutableStateOf(0f) }
    Slider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        onValueChangeFinished = { },
        valueRange = 0f..100f,
        steps = 1,
        modifier = modifier
            .requiredWidth(width = 412.dp)
            .requiredHeight(height = 892.dp)
            .background(color = Color.White))
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun MyProfilePreview() {
    MyProfile(Modifier)
}