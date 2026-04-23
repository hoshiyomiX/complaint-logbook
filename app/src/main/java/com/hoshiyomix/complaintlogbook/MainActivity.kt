package com.hoshiyomix.complaintlogbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hoshiyomix.complaintlogbook.ui.theme.MelastiDreamTheme
import com.hoshiyomix.complaintlogbook.ui.screens.MainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MelastiDreamTheme {
                MainScreen()
            }
        }
    }
}
