package com.example.mqtttelemetry

import android.util.Log
import com.github.avrokotlin.avro4k.Avro
import kotlinx.serialization.encodeToByteArray
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class MQTTClient(private val sslEnable: Boolean, private val brokerUrl: String, private val certificateStream: InputStream?,private val cacheDir: String) {

    private var mqttClient: MqttClient? = null

    fun startClient() {
        val clientId = MqttClient.generateClientId()
        mqttClient = MqttClient(this.brokerUrl, clientId, MqttDefaultFilePersistence(cacheDir))

        val options = MqttConnectOptions()
        options.isCleanSession = true
        options.connectionTimeout = 10  // seconds
        options.keepAliveInterval = 20  // seconds
        if (this.sslEnable)
            options.socketFactory = getSocketFactoryUsingBKS()

        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.i("MQTTClient", "Connection lost", cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(
                    "MQTTClient",
                    "Message arrived: Topic: $topic, Message: ${message?.toString()}"
                )
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTTClient", "Message Delivered")
            }
        })

        try {
            mqttClient?.connect(options)
            Log.d(
                "MQTTClient",
                "Connected to broker: $brokerUrl"
            )
            mqttClient?.subscribe("test/greeting", 1)
            Log.d("MQTTClient", "Subscribed to topic: test/greeting")
        } catch (e: MqttException) {
            Log.d("MQTTClient", "Failed to connect: ${e.message}")
        }

    }

    private fun getSocketFactoryUsingBKS(): SSLSocketFactory? {
        //val certificateStream: InputStream =
        //    resources.openRawResource(R.raw.ca) // replace 'your_certificate' with the certificate file name
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


    fun publishMessage(topic: String, message: MQTTMessage) {
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

}