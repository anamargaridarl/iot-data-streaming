# Iot Data Streaming flow

This repository presents a solution for streaming IoT data into a distributed event streaming platform.

![Component Diagram](diagram.png)

The solution leverages the MQTT protocol to transmit data from IoT devices to an internal engineering infrastructure. Devices publish data to an MQTT broker (NanoMQ), which Kafka Connect subscribes to in order to ingest the data into Kafka. During this process, the Confluent Schema Registry is used to validate message schemas.
