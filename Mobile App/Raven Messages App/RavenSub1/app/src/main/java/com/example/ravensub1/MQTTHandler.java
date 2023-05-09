package com.example.ravensub1;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hivemq.client.internal.mqtt.lifecycle.MqttClientAutoReconnectImpl;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class MQTTHandler {
    final String host = "";
    final String username = "";
    final String password = "";

    public String topic = "Alexa/Test";

    final Mqtt5BlockingClient client = MqttClient.builder()
            .useMqttVersion5()
            .serverHost(host)
            .serverPort(8883)
            .automaticReconnect(MqttClientAutoReconnectImpl.DEFAULT)
            .addConnectedListener(context -> System.out.println("connected " + LocalTime.now()))
            .addDisconnectedListener(context -> System.out.println("disconnected " + LocalTime.now()))
            .sslWithDefaultConfig()
            .buildBlocking();

    public void mqttConnectionStop(){
        client.disconnect();
    }

    public void subscribe(){
        // subscribe to the topic "my/test/topic"
        client.subscribeWith()
                .topicFilter(topic)
                .send();
    }

    public MQTTHandler(){
        // connect to HiveMQ Cloud with TLS and username/pw
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();
        System.out.println("Connected successfully");


        }

    public Mqtt5BlockingClient getClient() {
        return client;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        String newTopic = topic.toLowerCase();
        this.topic = "alexa/" + newTopic;
    }
}

