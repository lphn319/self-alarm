package hcmute.edu.vn.linhvalocvabao.selfalarmproject.models;

import java.io.Serializable;
import java.util.Calendar;

public class Event implements Serializable {
    private long id;
    private String title;
    private String description;
    private Calendar startTime;
    private Calendar endTime;
    private boolean hasAlarm;
    private int reminderMinutes;
    private String location;

    public Event() {
        this.startTime = Calendar.getInstance();
        this.endTime = Calendar.getInstance();
        this.hasAlarm = false;
        this.reminderMinutes = 15; // Default reminder time
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public boolean isHasAlarm() {
        return hasAlarm;
    }

    public void setHasAlarm(boolean hasAlarm) {
        this.hasAlarm = hasAlarm;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}