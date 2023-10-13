package Com.mariapublishers.mariaexecutive;

public class SchoolData {
    String id;
    String name;
    String price;
    String qty;

    public SchoolData() {
    }

    public SchoolData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public SchoolData(String bookId, String bookName, String rupees, String qty) {
        this.id = bookId;
        this.name = bookName;
        this.price = rupees;
        this.qty = qty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }
}
