package dk.statsbiblioteket.autonomous;

import java.util.LinkedList;
import java.util.List;

public class ResultCollector {
    private boolean success;
    private List<String> messages;


    public void setSuccess(boolean success) {
        this.success = success;
        messages = new LinkedList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public String toSummary(){
        StringBuilder builder = new StringBuilder();
        for (String message : messages) {
            builder.append(message).append("\n");
        }
        return builder.toString();
    }
}
