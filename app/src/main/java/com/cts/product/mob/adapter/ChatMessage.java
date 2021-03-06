package com.cts.product.mob.adapter;

import java.util.Date;

/**
 * Holds each chat message
 */

public class ChatMessage {
    //public static final int VIEW_TYPE_MESSAGE_SENT = 0;
    //public static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;
    public enum ChatDirection {
        Sent, Received
    }

    private String message;
    private String sender;
    //private boolean selfMessage = false;
    private ChatDirection direction;
    private Date createdAt;

    public ChatMessage() {
        createdAt = new Date();
    }

    public ChatMessage(String message, String sender) {
        this();
        this.message = message;
        this.sender = sender;
    }
    public ChatMessage(String message, ChatDirection direction) {
        this();
        this.message = message;
        this.direction = direction;
    }

    public int getType() {
        return direction.ordinal();
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /*public boolean isSelfMessage() {
        return selfMessage;
    }*/

}
