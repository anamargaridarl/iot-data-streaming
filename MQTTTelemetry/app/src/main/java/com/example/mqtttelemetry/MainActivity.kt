package com.example.mqtttelemetry

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mqtttelemetry.ui.theme.MQTTTelemetryTheme
import com.github.avrokotlin.avro4k.*
import kotlinx.serialization.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class MainActivity : ComponentActivity() {

    // MQTT client object
    private var mqttClient: MqttClient? = null
    private val brokerUrl = "ssl://10.0.2.2:8883"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MQTTTelemetryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        GreetingCall()
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        TelemetryCall()
                    }
                }
            }
        }

        // Initialize MQTT Client
        val clientId = MqttClient.generateClientId()  // Generate unique client ID
        mqttClient =
            MqttClient(brokerUrl, clientId, MqttDefaultFilePersistence(this.cacheDir.toString()))

        val options = MqttConnectOptions()
        options.isCleanSession = true
        options.connectionTimeout = 10  // Timeout in seconds
        options.keepAliveInterval = 20  // Keep alive in seconds
        options.socketFactory = getSocketFactoryUsingBKS()

        // Set up callback for message delivery and connection status
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e("MQTT", "Connection lost", cause)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connection Lost", Toast.LENGTH_SHORT).show()
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Message arrived: Topic: $topic, Message: ${message?.toString()}")
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Message: ${message?.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

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
            mqttClient?.subscribe("test/greeting", 1)
            mqttClient?.subscribe("test/telemetry", 1)
            runOnUiThread {
                Log.d("MQTT", "Subscribed to topic: test/greeting")
                Log.d("MQTT", "Subscribed to topic: test/telemetry")

            }
        } catch (e: MqttException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getSocketFactoryUsingBKS(): SSLSocketFactory? {
        val certificateStream: InputStream =
            resources.openRawResource(R.raw.ca) // replace 'your_certificate' with the certificate file name
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificateFactory.generateCertificate(certificateStream)

        // Create a KeyStore and load your certificate into it
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("server-cert", certificate)

        // Initialize a TrustManager that trusts the certificate
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.isEmpty()) {
            throw Exception("No TrustManagers found")
        }

        // Create an SSLContext and set the custom TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, null)
        return sslContext.socketFactory
    }


    // Publishing a message to a topic
    private fun publishMessage(topic: String, message: MQTTMessage) {
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
    fun GreetingCall() {
        Button(onClick = {
            Log.d("MQTT", "Greeting button clicked!")
            publishMessage("test/greeting", Greeting("Hello World!", "EN"))

        }) {
            Text("Greetings")
        }
    }


    @Composable
    fun TelemetryCall() {
        Button(onClick = {
            Log.d("MQTT", "Telemety button clicked!")
            publishMessage("test/telemetry", Telemetry("test_id", "merchant_id", "user_id", "date_time", "domain_id", "event_id", "payload"))

        }) {
            Text("Telemetry")
        }
    }

}



