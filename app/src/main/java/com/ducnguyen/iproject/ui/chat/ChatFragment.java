package com.ducnguyen.iproject.ui.chat;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ducnguyen.iproject.R;
import com.ducnguyen.iproject.ui.Message;
import com.ducnguyen.iproject.ui.MessageAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static android.view.View.generateViewId;

public class ChatFragment extends Fragment {
    private RecyclerView chatMessage;
    private MessageAdapter mMessageAdapter;
    private List<Message> mMessages;
    private TextInputEditText inputMessage;
    private ChatViewModel chatViewModel;
    private Switch autoSend;
    private ScrollView scrollChartLayout;
    private LinearLayout lineChartHolder;
    private List<String> listUUID;
    private Dictionary<String, Integer> lineChartTimer;

    private static Handler handler;
    private static Runnable autoSendMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        lineChartHolder = root.findViewById(R.id.lineChartHolder);
        chatMessage = root.findViewById(R.id.chatMessage);
        inputMessage = root.findViewById(R.id.inputMessage);
        autoSend = root.findViewById(R.id.autoSend);
        scrollChartLayout = root.findViewById(R.id.lineChart);

        createSubscribeCallback();

        lineChartTimer = new Hashtable<>();
        listUUID = new ArrayList<>();
        mMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getContext(),  mMessages);
        chatMessage.setAdapter(mMessageAdapter);

        //RecyclerView scroll vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        chatMessage.setLayoutManager(linearLayoutManager);

        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        FloatingActionButton btnSend = getActivity().findViewById(R.id.fab);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                publishMessage();
            }
        });

        setAutoSendCallback();

        return root;
    }

    @Override
    public void onDetach() {
        handler.removeCallbacksAndMessages(null);
        super.onDetach();
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
                drawChart(topic.substring(9, topic.indexOf(':')), topic.substring(topic.indexOf(':')+1), mqttMessage.toString());
                scrollView();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };

        chatViewModel.setSubscribeCallback(mqttSubscribeCallback).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean subscribeResult) {
                if (!subscribeResult) {
                    showToast("No connection to server!");
                }
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void scrollView() {
        chatMessage.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                chatMessage.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
            }
        });
    }

    private void drawChart(String topic, String deviceName, String message) {
        if (!listUUID.contains(topic)) {
            listUUID.add(topic);
            lineChartTimer.put(topic, 0);

            LineChart newLineChart = new LineChart(getContext());
            newLineChart.setLayoutParams(new ViewGroup.LayoutParams(lineChartHolder.getWidth(), 750));
            newLineChart.setId(UUID.fromString(topic).hashCode());
            newLineChart.getAxisRight().setEnabled(false);
            newLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            newLineChart.setTouchEnabled(true);
            newLineChart.setDragEnabled(true);
            newLineChart.setScaleEnabled(true);
            newLineChart.getDescription().setText(deviceName);

            List<Entry> entryList = new ArrayList<>();
            entryList.add(new Entry(0,0));

            LineDataSet lineDataSet = new LineDataSet(entryList,"value");
            lineDataSet.setColors(Color.BLUE);
            lineDataSet.setFillAlpha(110);
            LineData lineData = new LineData(lineDataSet);

            newLineChart.setData(lineData);

            newLineChart.getAxisLeft().setAxisMinimum(0);
            newLineChart.getAxisLeft().setAxisMaximum(255);
            newLineChart.getXAxis().setAxisMaximum(1000);
            newLineChart.setVisibleXRangeMaximum(20);

            newLineChart.invalidate();

            lineChartHolder.addView(newLineChart);
            scrollChartLayout.smoothScrollTo((int) scrollChartLayout.getX(), (int) scrollChartLayout.getY());
        }

        LineChart lineChart = getActivity().findViewById(UUID.fromString(topic).hashCode());

        LineData lineData = lineChart.getLineData();
        Integer lastTime = lineChartTimer.get(topic);

        Entry newEntry = new Entry(lastTime+1, Integer.parseInt(message));
        lineData.addEntry(newEntry, 0);

        if (lastTime >= 20) {
            lineChart.moveViewToX(lastTime - 18);
            if (lastTime > 1000 && lastTime % 1000 == 0) {
                lineChart.getXAxis().setAxisMaximum(1000 * (int) (lastTime / 1000 + 1));
            }
        }

        lineChart.invalidate();
        lineChartTimer.put(topic, lastTime+1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void publishMessage() {
        String payload = String.valueOf(inputMessage.getText());
        chatViewModel.publish(payload);
        inputMessage.setText("");
    }

    private void showToast(String text) {
        Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void setAutoSendCallback() {
        handler = new Handler(Looper.getMainLooper());

        autoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    autoSendMessage = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String payload = String.valueOf(new Random().nextInt(255));
                                chatViewModel.publish(payload);
                            } finally {
                                handler.postDelayed(autoSendMessage, 1000);
                            }
                        }
                    };

                    autoSendMessage.run();
                } else {
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });
    }

}