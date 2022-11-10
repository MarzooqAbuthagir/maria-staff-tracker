package Com.mariapublishers.mariaexecutive;

public class ChatMessage {
    private String messageText;
    private String chatDate;
    private String chatTime;
    private String dateTime;
    private String senderId;
    private String receiverId;

    public ChatMessage(String messageText, String chatDate, String chatTime, String dateTime, String senderId, String receiverId) {
        this.messageText = messageText;
        this.chatDate = chatDate;
        this.chatTime = chatTime;
        this.dateTime = dateTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getChatDate() {
        return chatDate;
    }

    public void setChatDate(String chatDate) {
        this.chatDate = chatDate;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}
