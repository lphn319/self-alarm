package hcmute.edu.vn.linhvalocvabao.selfalarmproject.models;

public class BlacklistContact {
    private String phoneNumber;
    private String name;
    private long dateAdded;
    private boolean blockCalls;
    private boolean blockMessages;

    public BlacklistContact() {
    }

    public BlacklistContact(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.dateAdded = System.currentTimeMillis();
        this.blockCalls = true;
        this.blockMessages = true;
    }

    // Getters và Setters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isBlockCalls() {
        return blockCalls;
    }

    public void setBlockCalls(boolean blockCalls) {
        this.blockCalls = blockCalls;
    }

    public boolean isBlockMessages() {
        return blockMessages;
    }

    public void setBlockMessages(boolean blockMessages) {
        this.blockMessages = blockMessages;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlacklistContact contact = (BlacklistContact) obj;
        return phoneNumber != null ? phoneNumber.equals(contact.phoneNumber) : contact.phoneNumber == null;
    }

    @Override
    public int hashCode() {
        return phoneNumber != null ? phoneNumber.hashCode() : 0;
    }
}