package org.dotspace.validation;

import java.util.List;
import java.util.Map;

public class ModelViolation {

    private String validationName;

    // private T model;

    private List<String> messages;
    
    private Map<String, Object> options;

    public String getValidationName() {
        return validationName;
    }

    public void setValidationName(String validationName) {
        this.validationName = validationName;
    }

    // public T getModel() {
    //     return model;
    // }

    // public void setModel(T model) {
    //     this.model = model;
    // }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
    
}
