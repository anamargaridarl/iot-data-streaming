package com.example.mqtttelemetry

import kotlinx.serialization.Serializable

interface MQTTMessage {}

@Serializable
data class Greeting(val greet: String, val language: String): MQTTMessage

@Serializable
data class Telemetry(val app_id: String, val merchant_id: String, val user_id: String, val date_time: String, val domain_id: String, val event_id: String, val event_payload: String): MQTTMessage
