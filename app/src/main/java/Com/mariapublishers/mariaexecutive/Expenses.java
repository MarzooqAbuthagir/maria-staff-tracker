package Com.mariapublishers.mariaexecutive;

public class Expenses {
    String expenseId;
    String amount;
    String description;
    String dateTime;

    String userId;
    String userName;

    String image;

    public Expenses(String expenseId, String amount, String description, String dateTime, String image) {
        this.expenseId = expenseId;
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
        this.image = image;
    }

    public Expenses(String expenseId, String amount, String description, String dateTime, String userId, String userName, String image) {
        this.expenseId = expenseId;
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
        this.userId = userId;
        this.userName = userName;
        this.image = image;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
