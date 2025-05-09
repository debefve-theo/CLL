package data.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Represents a delivery package (Colis) with associated details including
 * client name, delivery address, package number, current status, and
 * geographic coordinates.
 */
public class Colis implements Parcelable {
    private final String name;
    private final String address;
    private final int number;
    private int status;
    private final double latitude;
    private final double longitude;

    /**
     * Constructs a new Colis instance with the specified details.
     *
     * @param name      The name of the recipient or package
     * @param address   The full delivery address
     * @param number    The unique package number or ID
     * @param latitude  The latitude coordinate for the delivery location
     * @param longitude The longitude coordinate for the delivery location
     */
    public Colis(String name, String address, int number, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.number = number;
        this.status = -1;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructs a Colis instance from a Parcel.
     *
     * @param in the Parcel containing the serialized Colis data
     */
    protected Colis(Parcel in) {
        name = in.readString();
        address = in.readString();
        number = in.readInt();
        status = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    /**
     * Returns the recipient or package name.
     *
     * @return the name associated with this Colis
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the full delivery address.
     *
     * @return the address of this Colis
     */
    public String getAdresse() {
        return address;
    }

    /**
     * Returns the unique package number or ID.
     *
     * @return the package number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the current delivery status.
     *
     * @return the status string, or {@code null} if not set
     */
    public int getStatus() {
        return status;
    }

    /**
     * Updates the delivery status for this package.
     *
     * @param status the new status to assign
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Returns the latitude coordinate of the delivery location.
     *
     * @return the latitude value
     */
    public double getLatitude() {
        return latitude;
    }


    /**
     * Returns the longitude coordinate of the delivery location.
     *
     * @return the longitude value
     */
    public double getLongitude() {
        return longitude;
    }

    public static final Creator<Colis> CREATOR = new Creator<Colis>() {
        @Override
        public Colis createFromParcel(Parcel in) {
            return new Colis(in);
        }

        @Override
        public Colis[] newArray(int size) {
            return new Colis[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeInt(number);
        dest.writeInt(status);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

