package Com.mariapublishers.mariaexecutive;

import android.os.Parcel;
import android.os.Parcelable;

public class LatLngTracker implements Parcelable {
    double latitude;
    double longitude;
    String Name;

    public LatLngTracker(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        Name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(Name);
    }

    public LatLngTracker(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        Name = in.readString();
    }

    public static final Parcelable.Creator<LatLngTracker> CREATOR = new Parcelable.Creator<LatLngTracker>() {
        public LatLngTracker createFromParcel(Parcel in) {
            return new LatLngTracker(in);
        }

        public LatLngTracker[] newArray(int size) {
            return new LatLngTracker[size];
        }
    };
}
