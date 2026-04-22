package com.miles.fitnessagent;

public class Conversation {
    public final long id;
    public final String title;
    public final String updatedAt;

    public Conversation(long id, String title, String updatedAt) {
        this.id = id;
        this.title = title;
        this.updatedAt = updatedAt;
    }
}
