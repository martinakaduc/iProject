package com.ducnguyen.iproject.ui.publish;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ducnguyen.iproject.MQTTHelper;

public class PublishViewModel extends ViewModel {
    private final MQTTHelper mqttHelper;
    private final MutableLiveData<Boolean> publishResult = new MutableLiveData<Boolean>(false);

    public PublishViewModel() {
        mqttHelper = MQTTHelper.getHelper();
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

    public static String getTopic() {
        return MQTTHelper.getPublisherTopic();
    }

}