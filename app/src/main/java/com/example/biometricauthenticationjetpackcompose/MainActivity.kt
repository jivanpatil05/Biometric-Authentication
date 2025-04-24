package com.example.biometricauthenticationjetpackcompose

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.biometricauthenticationjetpackcompose.ui.theme.BiometricAuthenticationJetpackComposeTheme

class MainActivity : AppCompatActivity() {

    private val promptManager by lazy {
        BiometricPromptManager(this)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricAuthenticationJetpackComposeTheme {
                val biometricResult by promptManager.promptResults.collectAsState(initial = null)

                val enrollLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                    onResult = {
                        println("Activity result: $it")
                    }
                )

                LaunchedEffect(biometricResult) {
                    if(biometricResult is BiometricResult.AuthenticationNotSet) {
                        if(Build.VERSION.SDK_INT >= 30) {
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        promptManager.showBiometricPrompt(
                            title = "Sample prompt",
                            description = "Sample prompt description"
                        )
                    }) {
                        Text(text = "Authenticate")
                    }
                    biometricResult?.let { result ->
                        Text(
                            text = when(result) {
                                is BiometricResult.AuthenticationError -> {
                                    result.error
                                }
                                BiometricResult.AuthenticationFailed -> {
                                    "Authentication failed"
                                }
                                BiometricResult.AuthenticationNotSet -> {
                                    "Authentication not set"
                                }
                                BiometricResult.AuthenticationSuccess -> {
                                    "Authentication success"
                                }
                                BiometricResult.FeatureUnavailable -> {
                                    "Feature unavailable"
                                }
                                BiometricResult.HardwareUnavailable -> {
                                    "Hardware unavailable"
                                }
                            }
                        )

                    }
                }
            }
        }
    }
}