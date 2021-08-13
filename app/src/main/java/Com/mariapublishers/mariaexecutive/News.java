package Com.mariapublishers.mariaexecutive;

public class News {
    String newsId;
    String title;
    String message;
    String dateTime;

    public News(String newsId, String title, String message, String dateTime) {
        this.newsId = newsId;
        this.title = title;
        this.message = message;
        this.dateTime = dateTime;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
