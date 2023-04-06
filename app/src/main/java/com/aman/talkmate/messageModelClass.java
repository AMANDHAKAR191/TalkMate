package com.aman.talkmate;

public class messageModelClass {
    public static String SENT_BY_ME = "user";
    public static String SENT_BY_BOT="assistant";

    String role;
    String content;


    public messageModelClass() {
    }

    public messageModelClass(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

