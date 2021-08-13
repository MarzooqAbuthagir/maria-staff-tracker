package Com.mariapublishers.mariaexecutive;

public class AdminCheckInHistory {
    String name;
    String userId;
    String latitude;
    String longitude;
    String address;
    String description;
    String dateTime;
    String date;
    String checkoutDateTime;
    String Ischeckout;
    String State;
    String checkoutDesc;

    AdminCheckInHistory(){}

    public AdminCheckInHistory(String name, String userId, String latitude, String longitude, String address, String description, String dateTime, String date, String checkoutDateTime, String ischeckout, String state, String Checkoutdesc) {
        this.name = name;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;
        this.dateTime = dateTime;
        this.date = date;
        this.checkoutDateTime = checkoutDateTime;
        this.Ischeckout = ischeckout;
        this.State = state;
        this.checkoutDesc = Checkoutdesc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckoutDateTime() {
        return checkoutDateTime;
    }

    public void setCheckoutDateTime(String checkoutDateTime) {
        this.checkoutDateTime = checkoutDateTime;
    }

    public String getIscheckout() {
        return Ischeckout;
    }

    public void setIscheckout(String ischeckout) {
        Ischeckout = ischeckout;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCheckoutDesc() {
        return checkoutDesc;
    }

    public void setCheckoutDesc(String checkoutDesc) {
        this.checkoutDesc = checkoutDesc;
    }
}
