package org.projectexat.common;

import java.time.LocalDateTime;

public class Message {
    private String username;
    private String text;
    private LocalDateTime timestamp;

    public Message(String username, String text, LocalDateTime timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setUsername(String username) { this.username = username; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + username + ": " + text;
    }
}
