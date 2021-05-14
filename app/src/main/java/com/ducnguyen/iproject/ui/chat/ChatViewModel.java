package com.ducnguyen.iproject.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ducnguyen.iproject.MQTTHelper;

import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

public class ChatViewModel extends ViewModel {
    private final MQTTHelper mqttHelper;
    private final MutableLiveData<Boolean> subscribeResult = new MutableLiveData<Boolean>(false);
    private final MutableLiveData<Boolean> publishResult = new MutableLiveData<Boolean>(false);

    public ChatViewModel() {
        mqttHelper = MQTTHelper.getHelper();
    }

    public LiveData<Boolean> setSubscribeCallback(MqttCallbackExtended callback) {
        subscribeResult.setValue(true);

        try {
            mqttHelper.setCallback(callback);
        } catch (Exception ex) {
            subscribeResult.postValue(false);
            ex.printStackTrace();
        }

        return subscribeResult;
    }

    public LiveData<Boolean> publish(String payload) {
        publishResult.setValue(true);

        try {
            mqttHelper.publishToTopic(payload, false);
        } catch (Exception ex) {
            publishResult.postValue(false);
            ex.printStackTrace();
        }

        return publishResult;
    }

}