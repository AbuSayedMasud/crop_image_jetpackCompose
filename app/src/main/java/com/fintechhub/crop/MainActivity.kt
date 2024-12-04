package com.fintechhub.crop

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fintechhub.crop.presentation.ImagesViewModel
import com.fintechhub.crop.ui.t.ViewModelDemo
import com.fintechhub.crop.ui.theme.CropTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ImagesViewModel by viewModels()
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CropTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    App(viewModel)
                }
            }
        }
    }
}

@Composable
private fun App(viewModel: ImagesViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ViewModelDemo(viewModel = viewModel, modifier = Modifier.fillMaxSize())
//        SimpleDemo(modifier = Modifier.fillMaxSize())
    }
}