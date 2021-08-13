package Com.mariapublishers.mariaexecutive;

public class NearbyPlaces {
    String dateTime;
    String address;
    String latitude;
    String longitude;
    String name;
    public NearbyPlaces(String datetime, String address, String latitude, String longitude, String name) {
        this.dateTime = datetime;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
