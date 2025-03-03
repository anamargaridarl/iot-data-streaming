package com.example.mqtttelemetry

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mqtttelemetry.ui.theme.MQTTTheme
import java.io.InputStream
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    //if you want to estabilish an SSL connection
    private val sslBrokerUrl = "ssl://10.0.2.2:8883"
    private val brokerUrl = "tcp://10.0.2.2:1883"
    private var mqttClient: MQTTClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val context: Context = this
            MQTTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ButtonSwitcher(context)
                    }


                }
            }
        }

    }

    @Composable
    fun ButtonSwitcher(context: Context) {
        var visibleButton by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Show initial buttons if no selection is made
            if (visibleButton == null) {
                Button(onClick = { visibleButton = "buttonA"
                    val certificateStream: InputStream = resources.openRawResource(R.raw.ca)
                    mqttClient = MQTTClient(true, sslBrokerUrl, certificateStream, context.cacheDir.toString())
                    mqttClient!!.startClient();
                }) {
                    Text("SSL Enabled")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { visibleButton = "buttonB"
                    mqttClient = MQTTClient(false, brokerUrl, null, context.cacheDir.toString())
                    mqttClient!!.startClient();}) {
                    Text("No SSL")
                }
            } else {
                // Show selected button
                when (visibleButton) {
                    "buttonA" -> SecureGreeting()
                    "buttonB" -> Greeting()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back button to return to the initial state
                Button(onClick = { visibleButton = null }) {
                    Text("Back")
                }
            }
        }
    }
    @Composable
    fun SecureGreeting() {
        Button(
            onClick = {
                mqttClient?.publishMessage("test/greeting", Greeting("Hello World!", "EN"))
            },
        ) {
            Text("Secure Greeting")
        }
    }

    @Composable
    fun Greeting() {
        Button(
            onClick = {
                mqttClient?.publishMessage("test/greeting", Greeting("Hello World!", "EN"))
            },
        ) {
            Text("Greeting")
        }
    }
}




