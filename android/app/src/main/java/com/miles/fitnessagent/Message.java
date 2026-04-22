package com.miles.fitnessagent;

public class Message {
    public final String role;
    public String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
