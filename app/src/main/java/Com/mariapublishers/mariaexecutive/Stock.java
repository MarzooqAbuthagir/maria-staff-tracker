package Com.mariapublishers.mariaexecutive;

public class Stock {
    String orderIndex;
    String documentNo;
    String orderNo;
    String shipAddress;
    String bookingPlace;
    String bookingDate;
    String trichyToDispatchDate;

    String bookName, categoryName, totAmt, debit, orderDate;

    public Stock(String orderIndex, String documentNo, String orderNo, String shipAddress, String bookingPlace, String bookingDate, String trichyToDispatchDate, String orderDate) {
        this.orderIndex = orderIndex;
        this.documentNo = documentNo;
        this.orderNo = orderNo;
        this.shipAddress = shipAddress;
        this.bookingPlace = bookingPlace;
        this.bookingDate = bookingDate;
        this.trichyToDispatchDate = trichyToDispatchDate;
        this.orderDate = orderDate;
    }

    public Stock(String bookName, String categoryName, String totalAmount, String debit) {
        this.bookName = bookName;
        this.categoryName = categoryName;
        this.totAmt = totalAmount;
        this.debit = debit;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTotAmt() {
        return totAmt;
    }

    public void setTotAmt(String totAmt) {
        this.totAmt = totAmt;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(String orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(String shipAddress) {
        this.shipAddress = shipAddress;
    }

    public String getBookingPlace() {
        return bookingPlace;
    }

    public void setBookingPlace(String bookingPlace) {
        this.bookingPlace = bookingPlace;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getTrichyToDispatchDate() {
        return trichyToDispatchDate;
    }

    public void setTrichyToDispatchDate(String trichyToDispatchDate) {
        this.trichyToDispatchDate = trichyToDispatchDate;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}
