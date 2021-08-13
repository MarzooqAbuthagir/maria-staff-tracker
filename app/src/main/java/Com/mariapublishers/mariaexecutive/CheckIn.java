package Com.mariapublishers.mariaexecutive;

public class CheckIn {
    String dateTime;
    String address;
    String userId;
    String date;
    String CheckinId;
    String Ischeckout;
    String CheckoutDateTime;

    public CheckIn(String dateTime, String address, String userId, String date, String checkinId, String ischeckout, String checkoutDateTime) {
        this.dateTime = dateTime;
        this.address = address;
        this.userId = userId;
        this.date = date;
        this.CheckinId = checkinId;
        this.Ischeckout = ischeckout;
        this.CheckoutDateTime = checkoutDateTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckinId() {
        return CheckinId;
    }

    public void setCheckinId(String checkinId) {
        CheckinId = checkinId;
    }

    public String getIscheckout() {
        return Ischeckout;
    }

    public void setIscheckout(String ischeckout) {
        Ischeckout = ischeckout;
    }

    public String getCheckoutDateTime() {
        return CheckoutDateTime;
    }

    public void setCheckoutDateTime(String checkoutDateTime) {
        CheckoutDateTime = checkoutDateTime;
    }
}
