package com.example.mqtttelemetry

import kotlinx.serialization.Serializable

@Serializable
sealed class MQTTMessage

@Serializable
data class Greeting(val greet: String, val language: String): MQTTMessage()