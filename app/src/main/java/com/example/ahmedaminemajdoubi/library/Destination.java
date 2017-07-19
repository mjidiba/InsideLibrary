package com.example.ahmedaminemajdoubi.library;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AhmedAmineMajdoubi on 16/07/2017.
 */



public class Destination implements Parcelable{

    private int id;
    private double longitude;
    private double latitude;
    private int[] shelfs;

    public Destination() {

    }

    public Destination(int id, double longitude, double latitude) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.shelfs = new int[]{1, 2, 3};
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int[] getShelfs() {
        return shelfs;
    }

    public void setShelfs(int[] shelfs) {
        this.shelfs = shelfs;
    }

    public static final Creator<Destination> CREATOR = new Creator<Destination>() {
        public Destination createFromParcel(Parcel source) {
            Destination destination = new Destination();
            destination.id = source.readInt();
            destination.longitude = source.readDouble();
            destination.latitude = source.readDouble();
            destination.shelfs = source.createIntArray();
            return destination;
        }
        public Destination[] newArray(int size) {
            return new Destination[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeIntArray(shelfs);
    }
}

