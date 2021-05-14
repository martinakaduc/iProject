package com.ducnguyen.iproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MQTTHelper {
    private static String serverUrl = "";
    private static final String baseTopic = "iProject";
    private static final String clientId = UUID.randomUUID().toString();
    private static final String subscriptionTopic = baseTopic + "/#";
    private static final String publisherTopic = baseTopic + "/" + clientId + ":Essential Phone";

    private static MQTTHelper mqttHelper = null;
    @SuppressLint("StaticFieldLeak")
    private static MqttAndroidClient mqttAndroidClient;
    private static final MutableLiveData<Boolean> isConnected = new MutableLiveData<Boolean>(false);

    public MQTTHelper(Context context, String _serverUrl) {
        serverUrl = _serverUrl;
        mqttAndroidClient = new MqttAndroidClient(context, _serverUrl, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("MQTT", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    public static synchronized MQTTHelper getHelper(Context context, String _serverUrl) {
        if (mqttHelper==null) {
            mqttHelper = new MQTTHelper(context, _serverUrl);
        }
        return mqttHelper;
    }

    public static synchronized MQTTHelper getHelper() {
        return mqttHelper;
    }

    public static LiveData<Boolean> isConnected() {
        return isConnected;
    }

    public static synchronized void disconnect() {
        try {
            mqttAndroidClient.disconnect();
            mqttHelper = null;
            isConnected.postValue(false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public synchronized void connect(String username, String password){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isConnected.postValue(true);
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    disconnect();
                    Log.w("MQTT", "Failed to connect to: " + serverUrl + exception.toString());
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }

    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("MQTT", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishToTopic(String payload, boolean isRetain) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(isRetain);
            mqttAndroidClient.publish(publisherTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static String getPublisherTopic() {
        return publisherTopic;
    }
}

