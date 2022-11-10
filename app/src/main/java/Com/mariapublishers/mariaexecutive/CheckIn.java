package Com.mariapublishers.mariaexecutive;

public class CheckIn {
    String dateTime;
    String address;
    String userId;
    String date;
    String CheckinId;
    String Ischeckout;
    String CheckoutDateTime;
    String customerType;
    String customerName;
    String contactPerson;
    String notes;
    String contactNumber;
    String email;
    String website;
    String contactType;
    String additionalCus;

    public CheckIn(String dateTime, String address, String userId, String date, String checkinId, String ischeckout, String checkoutDateTime, String cusType, String customerName, String contactPerson, String notes, String contactNumber, String email, String website, String conType, String addCus) {
        this.dateTime = dateTime;
        this.address = address;
        this.userId = userId;
        this.date = date;
        this.CheckinId = checkinId;
        this.Ischeckout = ischeckout;
        this.CheckoutDateTime = checkoutDateTime;
        this.customerType = cusType;
        this.customerName = customerName;
        this.contactPerson = contactPerson;
        this.notes = notes;
        this.contactNumber = contactNumber;
        this.email = email;
        this.website = website;
        this.contactType = conType;
        this.additionalCus = addCus;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getAdditionalCus() {
        return additionalCus;
    }

    public void setAdditionalCus(String additionalCus) {
        this.additionalCus = additionalCus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
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
