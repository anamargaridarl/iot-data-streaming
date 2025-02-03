package com.example.mqtttelemetry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mqtttelemetry.ui.theme.MQTTTelemetryTheme
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Button
import java.nio.charset.StandardCharsets
import com.github.avrokotlin.avro4k.*
import kotlinx.serialization.*

@Serializable
data class Greeting(val greet: String, val language: String)

class MainActivity : ComponentActivity() {

    // MQTT client object
    private var mqttClient: MqttClient? = null
    private val brokerUrl = "tcp://10.0.2.2:1883"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MQTTTelemetryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }

        // Initialize MQTT Client
        val clientId = MqttClient.generateClientId()  // Generate unique client ID
        mqttClient = MqttClient(brokerUrl, clientId, MqttDefaultFilePersistence(this.cacheDir.toString()))

        val options = MqttConnectOptions()
        options.userName = "yourUsername"  // Optional
        options.password = "yourPassword".toCharArray()  // Optional
        options.isCleanSession = true
        options.connectionTimeout = 10  // Timeout in seconds
        options.keepAliveInterval = 20  // Keep alive in seconds

        // Set up callback for message delivery and connection status
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e("MQTT", "Connection lost", cause)
                runOnUiThread {
                Toast.makeText(this@MainActivity, "Connection Lost", Toast.LENGTH_SHORT).show()
            }}

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Message arrived: Topic: $topic, Message: ${message?.toString()}")
                runOnUiThread {
                Toast.makeText(this@MainActivity, "Message: ${message?.toString()}", Toast.LENGTH_SHORT).show()
            }}

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                runOnUiThread {

                    Log.d("MQTT", "Message Delivered")
                }
            }
        })

        // Connect to the MQTT broker
        try {
            mqttClient?.connect(options)
            Log.d("MQTT", "Connected to broker: $brokerUrl")
            runOnUiThread {
                Toast.makeText(this, "Connected to MQTT Broker", Toast.LENGTH_SHORT).show()
            }
            // Subscribe to a topic
            mqttClient?.subscribe("test/topic", 1)
            runOnUiThread {
                Log.d("MQTT", "Subscribed to topic: test/topic")
            }
        } catch (e: MqttException) {
            e.printStackTrace()
            runOnUiThread {
            Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_SHORT).show()
        }}

    }

    // Publishing a message to a topic
    private fun publishMessage(topic: String, message: Greeting) {
        try {
            val mqttMessage = MqttMessage(Avro.encodeToByteArray(message))
            mqttMessage.qos = 1  // Quality of Service level
            mqttMessage.isRetained = false
            mqttClient?.publish(topic, mqttMessage)
            Log.d("MQTT", "Message published to topic: $topic")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mqttClient?.disconnect()
            Log.d("MQTT", "Disconnected from broker")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Button(onClick = { publishMessage("test/topic", Greeting("Hello World!", "EN")) }) { }
    }

}


