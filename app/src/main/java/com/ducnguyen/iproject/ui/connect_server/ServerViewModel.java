package com.ducnguyen.iproject.ui.connect_server;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.ducnguyen.iproject.MQTTHelper;

public class ServerViewModel extends ViewModel {
    MQTTHelper mqttHelper = null;
    private String serverAddress = "";
    private String port = "";

    public ServerViewModel() {

    }

    public void setServerInfo(String _serverAddress, String _port) {
        serverAddress = _serverAddress;
        port = _port;
    }

    public void loginUser(Context context, String username, String password) {
        mqttHelper = MQTTHelper.getHelper(context, serverAddress + ":" + port);
        mqttHelper.connect(username, password);
    }

    public LiveData<Boolean> isServerConnected() {
        return MQTTHelper.isConnected();
    }

    public void disconnect() {
        MQTTHelper.disconnect();
    }
}