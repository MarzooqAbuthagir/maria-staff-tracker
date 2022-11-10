package Com.mariapublishers.mariaexecutive;

public class SpecimenData {
    private String categoryID;
    private String categoryName;
    private String bookID;
    private String bookName;
    private String noOfBooks;
    private String totAmt;

    public SpecimenData(String categoryID, String categoryName, String bookID, String bookName, String noOfBooks, String totAmt) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.bookID = bookID;
        this.bookName = bookName;
        this.noOfBooks = noOfBooks;
        this.totAmt = totAmt;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getNoOfBooks() {
        return noOfBooks;
    }

    public void setNoOfBooks(String noOfBooks) {
        this.noOfBooks = noOfBooks;
    }

    public String getTotAmt() {
        return totAmt;
    }

    public void setTotAmt(String totAmt) {
        this.totAmt = totAmt;
    }
}
