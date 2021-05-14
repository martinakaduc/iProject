package com.ducnguyen.iproject.ui.publish;

import android.app.Activity;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducnguyen.iproject.MainActivity;
import com.ducnguyen.iproject.R;
import com.ducnguyen.iproject.ui.Message;
import com.ducnguyen.iproject.ui.MessageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PublishFragment extends Fragment {
    private RecyclerView publishMessage;
    private MessageAdapter mMessageAdapter;
    private List<Message> mMessages;
    private TextInputEditText inputMessage;
    private PublishViewModel publishViewModel;
    private Switch autoSend;
    private static Handler handler;
    private static Runnable autoSendMessage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        publishViewModel =
                new ViewModelProvider(this).get(PublishViewModel.class);
        View root = inflater.inflate(R.layout.fragment_publish, container, false);

        publishMessage = root.findViewById(R.id.publishMessage);
        inputMessage = root.findViewById(R.id.inputMessage);
        autoSend = root.findViewById(R.id.autoSend);

        mMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getContext(),  mMessages);
        publishMessage.setAdapter(mMessageAdapter);

        //RecyclerView scroll vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        publishMessage.setLayoutManager(linearLayoutManager);

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

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void publishMessage() {
        String payload = String.valueOf(inputMessage.getText());
        publishViewModel.publish(payload);

        mMessages.add(new Message(PublishViewModel.getTopic(), Calendar.getInstance().getTime().toString(), payload));
        mMessageAdapter.notifyDataSetChanged();
        scrollView();

        inputMessage.setText("");
    }

    private void scrollView() {
        publishMessage.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                publishMessage.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
            }
        });
    }

    private void setAutoSendCallback() {
        handler = new Handler(Looper.getMainLooper());

        autoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    autoSendMessage = new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            try {
                                String payload = String.valueOf(new Random().nextInt(256));
                                publishViewModel.publish(payload);

                                mMessages.add(new Message(PublishViewModel.getTopic(), Calendar.getInstance().getTime().toString(), payload));
                                mMessageAdapter.notifyDataSetChanged();
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