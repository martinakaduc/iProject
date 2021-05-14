package com.ducnguyen.iproject.ui.subscribe;

import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducnguyen.iproject.MainActivity;
import com.ducnguyen.iproject.R;
import com.ducnguyen.iproject.ui.Message;
import com.ducnguyen.iproject.ui.MessageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SubscribeFragment extends Fragment {
    private RecyclerView subscribeMessage;
    private MessageAdapter mMessageAdapter;
    private List<Message> mMessages;
    private SubscribeViewModel subscribeViewModel;
    private int privateValue;
    private TextInputEditText input1;
    private TextInputEditText input2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        subscribeViewModel =
                new ViewModelProvider(this).get(SubscribeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_subscribe, container, false);

        subscribeMessage = root.findViewById(R.id.subscribeMessage);
        TextView textView = root.findViewById(R.id.textView);
        input1 = root.findViewById(R.id.input1);
        input2 = root.findViewById(R.id.input2);

        createSubscribeCallback();

        privateValue = 0;
        mMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getContext(),  mMessages);
        subscribeMessage.setAdapter(mMessageAdapter);

        //RecyclerView scroll vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        subscribeMessage.setLayoutManager(linearLayoutManager);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        Button button_add = root.findViewById(R.id.button_add);
        Button button_sub = root.findViewById(R.id.button_sub);
        Button button_mul = root.findViewById(R.id.button_mul);
        Button button_div = root.findViewById(R.id.button_div);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                privateValue += 1;
                textView.setText(String.valueOf(privateValue));
                showToast("This button is useless. Stop pressing it!");
            }
        });

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int in1 = Integer.parseInt(String.valueOf(input1.getText()));
                int in2 = Integer.parseInt(String.valueOf(input2.getText()));
                textView.setText(String.valueOf(in1+in2));
            }
        });

        button_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int in1 = Integer.parseInt(String.valueOf(input1.getText()));
                int in2 = Integer.parseInt(String.valueOf(input2.getText()));
                textView.setText(String.valueOf(in1-in2));
            }
        });

        button_mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int in1 = Integer.parseInt(String.valueOf(input1.getText()));
                int in2 = Integer.parseInt(String.valueOf(input2.getText()));
                textView.setText(String.valueOf(in1*in2));
            }
        });

        button_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int in1 = Integer.parseInt(String.valueOf(input1.getText()));
                int in2 = Integer.parseInt(String.valueOf(input2.getText()));
                textView.setText(String.valueOf(in1/in2));
            }
        });

        return root;
    }

    private void createSubscribeCallback() {
        MqttCallbackExtended mqttSubscribeCallback = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
//                showToast("Lost connection...");
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("MQTT", mqttMessage.toString());
                mMessages.add(new Message(topic, Calendar.getInstance().getTime().toString(), mqttMessage.toString()));
                mMessageAdapter.notifyDataSetChanged();
                scrollView();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };

        subscribeViewModel.setSubscribeCallback(mqttSubscribeCallback).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean subscribeResult) {
                if (!subscribeResult) {
                    showToast("No connection to server!");
                }
            }
        });

    }

    private void scrollView() {
        subscribeMessage.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                subscribeMessage.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
    }
}