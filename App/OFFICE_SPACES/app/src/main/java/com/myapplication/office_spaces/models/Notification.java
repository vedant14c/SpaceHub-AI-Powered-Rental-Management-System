package com.myapplication.office_spaces.models;

public class Notification {
    private Integer notificationId;
    private Integer userId;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private String createdAt;
    private Integer requestId;
    public Notification() {}

    public Notification(Integer userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public Integer getNotificationId() { return notificationId; }
    public Integer getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Boolean getIsRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
}