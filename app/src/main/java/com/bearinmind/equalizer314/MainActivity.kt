package com.bearinmind.equalizer314

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.bearinmind.equalizer314.ui.AudioFxAppScaffold
import com.bearinmind.equalizer314.ui.theme.AudioFxTheme
import com.bearinmind.equalizer314.ui.theme.StudioBackground
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AudioFxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AudioFxTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* permissions granted or denied handled gracefully */ }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf<String>()

                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioBackground
                ) {
                    AudioFxAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}
