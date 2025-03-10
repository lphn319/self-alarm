package hcmute.edu.vn.linhvalocvabao.selfalarmproject.models;

public class CallLogEntry {
    private String phoneNumber;
    private String callType;
    private String callDate;
    private String duration;

    public CallLogEntry(String phoneNumber, String callType, String callDate) {
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.callDate = callDate;
        this.duration = ""; // Default empty duration
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCallType() {
        return callType;
    }

    public String getCallDate() {
        return callDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}