package com.ducnguyen.iproject.ui;

public class Message {
    private final String topic;
    private final String time;
    private final String message;

    public Message(String _topic, String _time, String _message) {
        topic = _topic;
        time = _time;
        message = _message;
    }

    public String getTopic() {
        return topic;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
