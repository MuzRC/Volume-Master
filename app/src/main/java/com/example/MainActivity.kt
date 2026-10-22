package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.VolumeMainScreen
import com.example.ui.VolumeViewModel
import com.example.ui.theme.VolumeMasterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VolumeViewModel by viewModels()

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == "android.media.VOLUME_CHANGED_ACTION" ||
                action == AudioManager.RINGER_MODE_CHANGED_ACTION
            ) {
                viewModel.onSystemVolumeChanged()
            }
        }
    }

    private var isReceiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VolumeMasterTheme {
                VolumeMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction("android.media.VOLUME_CHANGED_ACTION")
                addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(volumeReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(volumeReceiver, filter)
            }
            isReceiverRegistered = true
        }
        viewModel.loadInitialData()
    }

    override fun onStop() {
        super.onStop()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(volumeReceiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            isReceiverRegistered = false
        }
    }
}
