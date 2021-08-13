package Com.mariapublishers.mariaexecutive;

public class Complains {
    String enquiryId;
    String description;
    String dateTime;

    String userId;
    String userName;

    public Complains(String enquiryId, String description, String dateTime) {
        this.enquiryId = enquiryId;
        this.description = description;
        this.dateTime = dateTime;
    }

    public Complains(String enquiryId, String description, String dateTime, String userId, String userName) {
        this.enquiryId = enquiryId;
        this.description = description;
        this.dateTime = dateTime;
        this.userId = userId;
        this.userName = userName;
    }

    public String getEnquiryId() {
        return enquiryId;
    }

    public void setEnquiryId(String enquiryId) {
        this.enquiryId = enquiryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
