package com.ducnguyen.iproject.ui.subscribe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ducnguyen.iproject.MQTTHelper;

import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

public class SubscribeViewModel extends ViewModel {
    private final MQTTHelper mqttHelper;
    private final MutableLiveData<Boolean> subscribeResult = new MutableLiveData<Boolean>(false);

    public SubscribeViewModel() {
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
}