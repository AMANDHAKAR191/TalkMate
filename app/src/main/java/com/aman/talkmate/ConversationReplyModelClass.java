package com.aman.talkmate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversationReplyModelClass {
    private final String role;
    private final String content;
    private final String message;

    public ConversationReplyModelClass(String input) {
        String pattern = "reply:\\{role:(.*),content:(.*)\\}\\n(.*)";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);

        if (m.matches()) {
            this.role = m.group(1);
            this.content = m.group(2);
            this.message = m.group(3);
        } else {
            this.role = "user";
            this.content = "";
            this.message = input;
        }
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getMessage() {
        return message;
    }
}