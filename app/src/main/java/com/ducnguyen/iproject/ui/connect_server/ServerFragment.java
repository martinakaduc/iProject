package com.ducnguyen.iproject.ui.connect_server;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ducnguyen.iproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import static androidx.core.content.ContextCompat.getSystemService;

public class ServerFragment extends Fragment {

    private ServerViewModel serverViewModel;
    private View serverInfoView;
    private View userInfoView;
    private TextInputEditText serverAddress;
    private TextInputEditText port;
    private TextInputEditText username;
    private TextInputEditText password;
    private TextView serverStatus;
    private final MutableLiveData<Boolean> internetConnected = new MutableLiveData<Boolean>(false);
    private final MutableLiveData<Boolean> autoConnect = new MutableLiveData<Boolean>(false);

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        serverViewModel =
                new ViewModelProvider(this).get(ServerViewModel.class);
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        View root = inflater.inflate(R.layout.fragment_connect, container, false);

        serverInfoView = root.findViewById(R.id.serverInfo);
        userInfoView = root.findViewById(R.id.userInfo);
        serverStatus = root.findViewById(R.id.serverStatus);

        serverInfoView.setVisibility(View.VISIBLE);
        userInfoView.setVisibility(View.INVISIBLE);

        serverAddress = serverInfoView.findViewById(R.id.serverAddress);
        port = serverInfoView.findViewById(R.id.port);
        username = userInfoView.findViewById(R.id.username);
        password = userInfoView.findViewById(R.id.password);

        final Button btnConnect = serverInfoView.findViewById(R.id.btnConnect);
        final Button btnDisconnect = serverInfoView.findViewById(R.id.btnDisconnect);
        final Button btnLogin = userInfoView.findViewById(R.id.btnLogin);
        final Button btnBack = userInfoView.findViewById(R.id.btnBack);

        btnConnect.setOnClickListener(this::onConnect);
        btnDisconnect.setOnClickListener(this::onDisconnect);
        btnLogin.setOnClickListener(this::onLogin);
        btnBack.setOnClickListener(this::onBack);

        setServerStatus();

        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                internetConnected.postValue(true);
            }

            @Override
            public void onLost(Network network) {
                internetConnected.postValue(false);
            }
        });

        observeNetwork();

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("This button is useless. Stop pressing it!");
            }
        });

        return root;
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private LiveData<Boolean> getInternetState() {
        return internetConnected;
    }

    private LiveData<Boolean> getAutoConnect() {
        return autoConnect;
    }

    private void onConnect(View view) {
        String _serverAddress = String.valueOf(serverAddress.getText());
        String _port = String.valueOf(port.getText());
        serverViewModel.setServerInfo(_serverAddress, _port);

        serverInfoView.setVisibility(View.INVISIBLE);
        userInfoView.setVisibility(View.VISIBLE);
    }

    private void onDisconnect(View view) {
        showToast("Disconnecting...");
        serverViewModel.disconnect();
        setServerStatus();
    }

    private void onLogin(View view) {
        String _username = String.valueOf(username.getText());
        String _password = String.valueOf(password.getText());
        showToast("Connecting...");

        serverViewModel.loginUser(this.getActivity().getApplicationContext(), _username, _password);
    }

    private void onBack(View view) {
        serverInfoView.setVisibility(View.VISIBLE);
        userInfoView.setVisibility(View.INVISIBLE);
    }

    private void setServerStatus() {
        serverViewModel.isServerConnected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                autoConnect.postValue(success);
                setStatus(success);
            }
        });
    }

    private void observeNetwork() {
        getInternetState().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasInternet) {
                if (!hasInternet) {
                    setStatus(hasInternet);
                } else {
                    getAutoConnect().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean autoConnect) {
                            if (autoConnect) {
                                setStatus(autoConnect);
                            }
                        }
                    });
                }
            }
        });
    }

    private void setStatus(boolean status) {
        if (status) {
            serverStatus.setBackgroundColor(Color.parseColor("#FF008000"));
            serverStatus.setText("Server: Connected");

        } else {
            serverStatus.setBackgroundColor(Color.DKGRAY);
            serverStatus.setText("Server: Not Connect");

        }
    }

    private void showToast(String text) {
        Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
    }

}